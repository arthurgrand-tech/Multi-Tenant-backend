package com.ArthurGrand.admin.tenants.serviceImp;

import com.ArthurGrand.admin.dto.TenantRegisterDto;
import com.ArthurGrand.admin.dto.TenantResponseDto;
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
import jakarta.transaction.Transactional;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
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
    private final ApplicationEventPublisher eventPublisher;
    private final TenantCacheService tenantCacheService;

    public TenantServiceImp(JdbcTemplate jdbcTemplate,
                            DatabaseConfiguration databaseConfiguration,
                            TenantRepository tenantRepository,
                            ModelMapper modelMapper,
                            TenantProfileRepository tenantProfileRepository,
                            ApplicationEventPublisher eventPublisher,
                            TenantCacheService tenantCacheService) {
        this.jdbcTemplate = jdbcTemplate;
        this.databaseConfiguration = databaseConfiguration;
        this.tenantRepository = tenantRepository;
        this.modelMapper = modelMapper;
        this.tenantProfileRepository=tenantProfileRepository;
        this.eventPublisher=eventPublisher;
        this.tenantCacheService=tenantCacheService;
    }

    @Override
    @Transactional
    public TenantResponseDto createTenant(TenantRegisterDto tenantRegisterDto) throws IllegalArgumentException {
        try {
            // Uniqueness checks
            if (tenantRepository.findByDomain(tenantRegisterDto.getDomain()).isPresent()) {
                throw new IllegalArgumentException("Domain already exists.");
            }
            if (tenantRepository.findByCompanyName(tenantRegisterDto.getCompanyName()).isPresent()) {
                throw new IllegalArgumentException("Company name already exists.");
            }
            if (tenantRepository.findByDatabaseName(tenantRegisterDto.getDomain()).isPresent()) {
                throw new IllegalArgumentException("Database name already exists.");
            }
            if (tenantRepository.findByAdminEmail(tenantRegisterDto.getAdminEmail()).isPresent()) {
                throw new IllegalArgumentException("Admin email already exists.");
            }
            // Manually map DTO to Tenant
            Tenant tenant = new Tenant();
            tenant.setDomain(tenantRegisterDto.getDomain());
            tenant.setCompanyName(tenantRegisterDto.getCompanyName());
            tenant.setAdminEmail(tenantRegisterDto.getAdminEmail());
            tenant.setUsesCustomDb(tenantRegisterDto.getUsesCustomDb());
            tenant.setDbHost(tenantRegisterDto.getDbHost());
            tenant.setDbPort(tenantRegisterDto.getDbPort());
            tenant.setDbUsername(tenantRegisterDto.getDbUsername());
            tenant.setDbPassword(tenantRegisterDto.getDbPassword());
            tenant.setDatabaseName(tenantRegisterDto.getDomain()); // Use domain as DB name
            tenant.setTimezone(tenantRegisterDto.getTimezone());
            tenant.setCountry(tenantRegisterDto.getCountry());
            Tenant savedTenant = tenantRepository.save(tenant);

            TenantResponseDto tenantResDto = modelMapper.map(savedTenant, TenantResponseDto.class);

            TenantProfile tenantProfile= new TenantProfile();
            tenantProfile.setTenant(savedTenant);
            tenantProfile.setContactPerson(tenantRegisterDto.getContactPerson());
            tenantProfile.setEmail(tenantRegisterDto.getAdminEmail());
            tenantProfile.setPhoneNumber(tenantRegisterDto.getPhoneNumber());
            tenantProfile.setAddress(tenantRegisterDto.getAddress());
            tenantProfile.setWebsite(tenantRegisterDto.getWebsite());
            TenantProfile savedTenantProfile=tenantProfileRepository.save(tenantProfile);

            // After saving tenant and profile
            EmailTemplateBindingDTO binding = new EmailTemplateBindingDTO();
            binding.setContactPerson(savedTenantProfile.getContactPerson());
            binding.setOrganizationName(savedTenant.getCompanyName());
            binding.setDomain(savedTenant.getDomain());
            binding.setAdminEmail(savedTenantProfile.getEmail());
            binding.setPageUrl("http://localhost:5000/" + savedTenant.getDomain() + "/login");

            // Publish event (this will be handled asynchronously)
            eventPublisher.publishEvent(new NotificationEvent(
                    savedTenant.getTenantId(),
                    savedTenant.getAdminEmail(),
                    "Tenant Registration",
                    "Tenant registration successful.",
                    "tenant-created",
                    binding,
                    EmailCategory.TenantCreate
            ));
            return tenantResDto;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
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
            // Check if DB exists (implement your own logic here)
            boolean dbExists = checkIfDatabaseExists(tenant.getDatabaseName());

            if (!dbExists) {
                createTenantDatabase(tenant.getDatabaseName());
            }

            String tenantUrl = buildJdbcUrl(tenant);
            //String tenantUrl = tenant.getJdbcUrl("localhost", 3306); // fallback to provider's DB

            String username = tenant.isUsesCustomDb() ? tenant.getDbUsername() : dbUsername;
            String password = tenant.isUsesCustomDb() ? tenant.getDbPassword() : dbPassword;

            databaseConfiguration.addTenant(tenant.getDatabaseName(), tenantUrl, username, password);
            migrateTenantSchema(tenantUrl, username, password);

            // Update status to ACTIVE
            tenant.setStatus(TenantStatus.ACTIVE);
            Tenant savedTenant=tenantRepository.save(tenant);

            tenantCacheService.updateTenantCache(savedTenant); // 🔁 manually update cache

            Optional<TenantProfile> tenantProfileOpt=tenantProfileRepository.findByTenant_TenantId(savedTenant.getTenantId());

            // After saving tenant and profile
            EmailTemplateBindingDTO binding = new EmailTemplateBindingDTO();
            binding.setOrganizationName(savedTenant.getCompanyName());
            binding.setContactPerson(tenantProfileOpt.get().getContactPerson());
            binding.setPageUrl("http://localhost:5000/");

            // Publish event (this will be handled asynchronously)
            eventPublisher.publishEvent(new NotificationEvent(
                    savedTenant.getTenantId(),
                    savedTenant.getAdminEmail(),
                    "Tenant Activation",
                    "Tenant activation successful.",
                    "tenant-activate",
                    binding,
                    EmailCategory.TenantActive
            ));
            return modelMapper.map(tenant, TenantResponseDto.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to activate tenant: " + e.getMessage(), e);
        }
    }

    public boolean checkIfDatabaseExists(String dbName) {
        String sql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?";
        List<String> result = jdbcTemplate.queryForList(sql, String.class, dbName);
        return !result.isEmpty();
    }

    public String buildJdbcUrl(Tenant tenant) {
        String host = tenant.isUsesCustomDb() ? tenant.getDbHost() : "localhost";
        int port = tenant.isUsesCustomDb() ? tenant.getDbPort() : 3306;
        return "jdbc:mysql://" + host + ":" + port + "/" + tenant.getDatabaseName()
                + "?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";
    }

    private void createTenantDatabase(String databaseName) {
        jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS " + databaseName);
    }

    private void migrateTenantSchema(String url, String username, String password) {
        try {
            // Extract database name from URL
            String databaseName = getDatabaseNameFromUrl(url);

            // Create connection configuration
            Flyway flyway = Flyway.configure()
                    .dataSource(url, username, password)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .schemas(databaseName)
                    .createSchemas(true)
                    .load();

            // Run migration
            flyway.migrate();

        } catch (FlywayException e) {
            System.err.println("Flyway migration failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Flyway migration failed for tenant DB: " + url, e);
        }
    }

    private String getDatabaseNameFromUrl(String url) {
        // Extract database name from URL
        String[] parts = url.split("/");
        String dbPart = parts[parts.length - 1];
        return dbPart.split("\\?")[0];
    }

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
        return modelMapper.map(tenants, List.class);
    }
}