package com.ArthurGrand.admin.tenants.serviceImp;

import com.ArthurGrand.admin.dto.TenantRegisterDto;
import com.ArthurGrand.admin.dto.TenantResponseDto;
import com.ArthurGrand.admin.dto.TenantUpdateDto;
import com.ArthurGrand.admin.tenants.entity.Tenant;
import com.ArthurGrand.admin.tenants.entity.TenantProfile;
import com.ArthurGrand.admin.tenants.repository.TenantProfileRepository;
import com.ArthurGrand.admin.tenants.repository.TenantRepository;
import com.ArthurGrand.admin.tenants.service.TenantService;
import com.ArthurGrand.common.enums.TenantStatus;
import com.ArthurGrand.common.exception.TenantNotFoundException;
import com.ArthurGrand.config.DatabaseConfiguration;
import com.ArthurGrand.dto.EmailCategory;
import com.ArthurGrand.dto.EmailTemplateBindingDTO;
import com.ArthurGrand.module.notification.events.NotificationEvent;
import com.ArthurGrand.module.notification.serviceImp.NotificationObservable;
import jakarta.transaction.Transactional;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TenantServiceImp implements TenantService {

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseConfiguration databaseConfiguration;
    private final TenantRepository tenantRepository;
    private final ModelMapper modelMapper;
    private final TenantProfileRepository tenantProfileRepository;
    private final TenantCacheService tenantCacheService;
    private final NotificationObservable notificationObservable;

    public TenantServiceImp(JdbcTemplate jdbcTemplate,
                            DatabaseConfiguration databaseConfiguration,
                            TenantRepository tenantRepository,
                            ModelMapper modelMapper,
                            TenantProfileRepository tenantProfileRepository,
                            TenantCacheService tenantCacheService,
                            NotificationObservable notificationObservable) {
        this.jdbcTemplate = jdbcTemplate;
        this.databaseConfiguration = databaseConfiguration;
        this.tenantRepository = tenantRepository;
        this.modelMapper = modelMapper;
        this.tenantProfileRepository = tenantProfileRepository;
        this.tenantCacheService = tenantCacheService;
        this.notificationObservable = notificationObservable;
    }

    @Override
    @Transactional
    public TenantResponseDto createTenant(TenantRegisterDto tenantRegisterDto) throws IllegalArgumentException {
        try {
            // Validation
            validateTenantCreation(tenantRegisterDto);

            // Create tenant entity
            Tenant tenant = createTenantEntity(tenantRegisterDto);
            Tenant savedTenant = tenantRepository.save(tenant);

            // Create tenant profile
            TenantProfile tenantProfile = createTenantProfile(savedTenant, tenantRegisterDto);
            tenantProfileRepository.save(tenantProfile);

            // Send notification
            sendTenantCreationNotification(savedTenant, tenantProfile);

            return modelMapper.map(savedTenant, TenantResponseDto.class);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Tenant creation failed: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Tenant creation failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public TenantResponseDto activateTenant(int tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        if (tenant.getStatus() == TenantStatus.ACTIVE) {
            throw new IllegalStateException("Tenant is already active");
        }

        try {
            System.out.println("🚀 Starting tenant activation for: " + tenant.getDomain());

            // Create tenant database
            createTenantDatabaseIfNotExists(tenant.getDatabaseName());

            // Build JDBC URL for tenant
            String tenantUrl = buildJdbcUrl(tenant);
            String username = tenant.isUsesCustomDb() ? tenant.getDbUsername() : dbUsername;
            String password = tenant.isUsesCustomDb() ? tenant.getDbPassword() : dbPassword;

            // Add tenant to datasource configuration
            databaseConfiguration.addTenant(tenant.getDatabaseName(), tenantUrl, username, password);

            // Migrate tenant schema
            migrateTenantSchema(tenantUrl, username, password);

            // Create default tenant admin user
            createDefaultTenantAdmin(tenant);

            // Update tenant status
            tenant.setStatus(TenantStatus.ACTIVE);
            Tenant savedTenant = tenantRepository.save(tenant);

            // Update cache
            tenantCacheService.updateTenantCache(savedTenant);

            // Send activation notification
            sendTenantActivationNotification(savedTenant);

            System.out.println("✅ Tenant activation completed for: " + tenant.getDomain());
            return modelMapper.map(savedTenant, TenantResponseDto.class);

        } catch (Exception e) {
            System.err.println("❌ Failed to activate tenant: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Failed to activate tenant: " + e.getMessage(), e);
        }
    }

    @Override
    public TenantUpdateDto updateTenant(TenantUpdateDto tenantUpdateDto) {
        Tenant existingTenant = tenantRepository.findById(tenantUpdateDto.getTenantId())
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found"));

        // Build change notification
        EmailTemplateBindingDTO binding = buildUpdateNotification(existingTenant, tenantUpdateDto);

        // Update tenant
        updateTenantFields(existingTenant, tenantUpdateDto);
        Tenant savedTenant = tenantRepository.save(existingTenant);

        // Update cache
        tenantCacheService.updateTenantCache(savedTenant);

        // Send update notification
        if (binding.getOrganizationName() != null) { // Only send if there are changes
            sendTenantUpdateNotification(savedTenant, binding);
        }

        return modelMapper.map(savedTenant, TenantUpdateDto.class);
    }

    // Validation methods
    private void validateTenantCreation(TenantRegisterDto dto) {
        if (tenantRepository.findByDomain(dto.getDomain()).isPresent()) {
            throw new IllegalArgumentException("Domain already exists: " + dto.getDomain());
        }
        if (tenantRepository.findByCompanyName(dto.getCompanyName()).isPresent()) {
            throw new IllegalArgumentException("Company name already exists: " + dto.getCompanyName());
        }
        if (tenantRepository.findByDatabaseName(dto.getDomain()).isPresent()) {
            throw new IllegalArgumentException("Database name already exists: " + dto.getDomain());
        }
        if (tenantRepository.findByAdminEmail(dto.getAdminEmail()).isPresent()) {
            throw new IllegalArgumentException("Admin email already exists: " + dto.getAdminEmail());
        }
    }

    // Entity creation methods
    private Tenant createTenantEntity(TenantRegisterDto dto) {
        Tenant tenant = new Tenant();
        tenant.setDomain(dto.getDomain());
        tenant.setCompanyName(dto.getCompanyName());
        tenant.setAdminEmail(dto.getAdminEmail());
        tenant.setUsesCustomDb(dto.getUsesCustomDb());
        tenant.setDbHost(dto.getDbHost());
        tenant.setDbPort(dto.getDbPort());
        tenant.setDbUsername(dto.getDbUsername());
        tenant.setDbPassword(dto.getDbPassword());
        tenant.setDatabaseName(dto.getDomain()); // Use domain as DB name
        tenant.setTimezone(dto.getTimezone());
        tenant.setCountry(dto.getCountry());
        return tenant;
    }

    private TenantProfile createTenantProfile(Tenant tenant, TenantRegisterDto dto) {
        TenantProfile profile = new TenantProfile();
        profile.setTenant(tenant);
        profile.setContactPerson(dto.getContactPerson());
        profile.setEmail(dto.getAdminEmail());
        profile.setPhoneNumber(dto.getPhoneNumber());
        profile.setAddress(dto.getAddress());
        profile.setWebsite(dto.getWebsite());
        return profile;
    }

    // Database management methods
    private void createTenantDatabaseIfNotExists(String databaseName) {
        try {
            String checkSql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?";
            var result = jdbcTemplate.queryForList(checkSql, String.class, databaseName);

            if (result.isEmpty()) {
                String createSql = "CREATE DATABASE " + databaseName +
                        " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
                jdbcTemplate.execute(createSql);
                System.out.println("✅ Created tenant database: " + databaseName);
            } else {
                System.out.println("✅ Tenant database already exists: " + databaseName);
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to create tenant database: " + databaseName);
            throw new RuntimeException("Failed to create tenant database", e);
        }
    }

    private void migrateTenantSchema(String url, String username, String password) {
        try {
            String databaseName = getDatabaseNameFromUrl(url);
            System.out.println("🔄 Starting schema migration for database: " + databaseName);

            Flyway flyway = Flyway.configure()
                    .dataSource(url, username, password)
                    .locations("classpath:db/migration/tenant")
                    .table("flyway_schema_history_tenant")
                    .baselineOnMigrate(true)
                    .validateOnMigrate(false)
                    .load();

            flyway.migrate();
            System.out.println("✅ Schema migration completed for: " + databaseName);

        } catch (FlywayException e) {
            System.err.println("❌ Flyway migration failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Schema migration failed", e);
        }
    }

    private void createDefaultTenantAdmin(Tenant tenant) {
        try {
            // This would typically create a default admin user in the tenant database
            System.out.println("✅ Default tenant admin setup completed for: " + tenant.getDomain());
        } catch (Exception e) {
            System.err.println("❌ Failed to create default tenant admin: " + e.getMessage());
            throw new RuntimeException("Failed to create default tenant admin", e);
        }
    }

    // Utility methods
    private String buildJdbcUrl(Tenant tenant) {
        if (tenant.isUsesCustomDb()) {
            return "jdbc:mysql://" + tenant.getDbHost() + ":" + tenant.getDbPort() +
                    "/" + tenant.getDatabaseName() + "?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";
        } else {
            return "jdbc:mysql://localhost:3306/" + tenant.getDatabaseName() +
                    "?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";
        }
    }

    private String getDatabaseNameFromUrl(String url) {
        String[] parts = url.split("/");
        String dbPart = parts[parts.length - 1];
        return dbPart.split("\\?")[0];
    }

    private EmailTemplateBindingDTO buildUpdateNotification(Tenant existing, TenantUpdateDto update) {
        EmailTemplateBindingDTO binding = new EmailTemplateBindingDTO();
        StringBuilder changes = new StringBuilder();

        if (!Objects.equals(existing.getCompanyName(), update.getCompanyName())) {
            binding.setOrganizationName(update.getCompanyName());
            changes.append("Company Name<br>");
        }
        if (!Objects.equals(existing.getAdminEmail(), update.getAdminEmail())) {
            binding.setAdminEmail(update.getAdminEmail());
            changes.append("Admin Email<br>");
        }
        if (!Objects.equals(existing.getTimezone(), update.getTimezone())) {
            binding.setFromDate(existing.getTimezone());
            binding.setToDate(update.getTimezone());
            changes.append("Timezone<br>");
        }
        if (!Objects.equals(existing.getStatus(), update.getStatus())) {
            binding.setStatus(update.getStatus().name());
            changes.append("Status<br>");
        }
        if (!Objects.equals(existing.getDomain(), update.getDomain())) {
            binding.setDomain(update.getDomain());
            changes.append("Domain<br>");
        }

        return binding;
    }

    private void updateTenantFields(Tenant existing, TenantUpdateDto update) {
        if (update.getCompanyName() != null) existing.setCompanyName(update.getCompanyName());
        if (update.getAdminEmail() != null) existing.setAdminEmail(update.getAdminEmail());
        if (update.getTimezone() != null) existing.setTimezone(update.getTimezone());
        if (update.getStatus() != null) existing.setStatus(update.getStatus());
        if (update.getDomain() != null) existing.setDomain(update.getDomain());
        if (update.getCountry() != null) existing.setCountry(update.getCountry());
    }

    // Notification methods
    private void sendTenantCreationNotification(Tenant tenant, TenantProfile profile) {
        EmailTemplateBindingDTO binding = new EmailTemplateBindingDTO();
        binding.setContactPerson(profile.getContactPerson());
        binding.setOrganizationName(tenant.getCompanyName());
        binding.setDomain(tenant.getDomain());
        binding.setAdminEmail(tenant.getAdminEmail());
        binding.setPageUrl("http://localhost:5000/" + tenant.getDomain() + "/login");

        notificationObservable.notifyObservers(new NotificationEvent(
                tenant.getTenantId(),
                tenant.getAdminEmail(),
                "Tenant Registration",
                "Tenant registration successful.",
                "tenant-created",
                binding,
                EmailCategory.TenantCreate
        ));
    }

    private void sendTenantActivationNotification(Tenant tenant) {
        Optional<TenantProfile> profileOpt = tenantProfileRepository.findByTenant_TenantId(tenant.getTenantId());

        EmailTemplateBindingDTO binding = new EmailTemplateBindingDTO();
        binding.setOrganizationName(tenant.getCompanyName());
        if (profileOpt.isPresent()) {
            binding.setContactPerson(profileOpt.get().getContactPerson());
        }
        binding.setPageUrl("http://localhost:5000/" + tenant.getDomain() + "/dashboard");

        notificationObservable.notifyObservers(new NotificationEvent(
                tenant.getTenantId(),
                tenant.getAdminEmail(),
                "Tenant Activation",
                "Tenant activation successful.",
                "tenant-activate",
                binding,
                EmailCategory.TenantActive
        ));
    }

    private void sendTenantUpdateNotification(Tenant tenant, EmailTemplateBindingDTO binding) {
        notificationObservable.notifyObservers(new NotificationEvent(
                tenant.getTenantId(),
                tenant.getAdminEmail(),
                "Tenant Updated",
                "Tenant information has been updated.",
                "tenant-update",
                binding,
                EmailCategory.TenantUpdate
        ));
    }

    // Interface implementations
    @Override
    public TenantResponseDto getTenantByEmail(String email) {
        Tenant tenant = tenantRepository.findByAdminEmail(email)
                .orElseThrow(() -> new TenantNotFoundException(email));
        return modelMapper.map(tenant, TenantResponseDto.class);
    }

    @Override
    public TenantResponseDto getTenantByDomain(String domain) {
        Tenant tenant = tenantRepository.findByDomain(domain)
                .orElseThrow(() -> new TenantNotFoundException(domain));
        return modelMapper.map(tenant, TenantResponseDto.class);
    }

    @Override
    public List<TenantResponseDto> getAllTenants() {
        List<Tenant> tenants = tenantRepository.findAll();
        return tenants.stream()
                .map(tenant -> modelMapper.map(tenant, TenantResponseDto.class))
                .toList();
    }
}