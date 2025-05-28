package com.ArthurGrand.admin.tenants.serviceImp;

import com.ArthurGrand.admin.dto.TenantCreateDto;
import com.ArthurGrand.admin.dto.TenantResponseDto;
import com.ArthurGrand.admin.tenants.entity.Tenant;
import com.ArthurGrand.admin.tenants.repository.TenantRepository;
import com.ArthurGrand.admin.tenants.service.TenantService;
import com.ArthurGrand.common.exception.TenantNotFoundException;
import com.ArthurGrand.config.DatabaseConfiguration;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantServiceImp implements TenantService {

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseConfiguration databaseConfiguration;
    private final TenantRepository tenantRepository;
    private final ModelMapper modelMapper;

    public TenantServiceImp(JdbcTemplate jdbcTemplate,
                            DatabaseConfiguration databaseConfiguration,
                            TenantRepository tenantRepository,
                            ModelMapper modelMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.databaseConfiguration = databaseConfiguration;
        this.tenantRepository = tenantRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public TenantResponseDto createTenant(TenantCreateDto tenantCreateDto) throws IllegalArgumentException {
        try {
            // Uniqueness checks
            if (tenantRepository.findByDomain(tenantCreateDto.getDomain()).isPresent()) {
                throw new IllegalArgumentException("Domain already exists.");
            }

            if (tenantRepository.findByCompanyName(tenantCreateDto.getCompanyName()).isPresent()) {
                throw new IllegalArgumentException("Company name already exists.");
            }

            if (tenantRepository.findByDatabaseName(tenantCreateDto.getDatabaseName()).isPresent()) {
                throw new IllegalArgumentException("Database name already exists.");
            }

            if (tenantRepository.findByAdminEmail(tenantCreateDto.getAdminEmail()).isPresent()) {
                throw new IllegalArgumentException("Admin email already exists.");
            }

            // Create tenant metadata
            Tenant tenant = modelMapper.map(tenantCreateDto, Tenant.class);

            // Create tenant database
            createTenantDatabase(tenant.getDatabaseName());

            // Construct a clean JDBC URL
            String tenantUrl = "jdbc:mysql://localhost/" + tenant.getDatabaseName() + "?serverTimezone=UTC&useSSL=false";

            // Register tenant in multi-tenant configuration
            databaseConfiguration.addTenant(tenant.getDatabaseName(), tenantUrl, dbUsername, dbPassword);

            // Run Flyway migration
            migrateTenantSchema(tenantUrl, dbUsername, dbPassword);

            // Save metadata
            Tenant savedTenant = tenantRepository.save(tenant);
            return modelMapper.map(savedTenant, TenantResponseDto.class);

        } catch (IllegalArgumentException e) {
            throw e; // propagate to controller
        } catch (Exception e) {
            throw new IllegalStateException("Tenant creation failed: " + e.getMessage(), e);
        }
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