package com.ArthurGrand.common.component;

import com.ArthurGrand.admin.tenants.entity.Tenant;
import com.ArthurGrand.admin.tenants.repository.TenantRepository;
import com.ArthurGrand.common.enums.TenantStatus;
import com.ArthurGrand.config.DatabaseConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Component
public class TenantDataSourceInitializer {
    private final TenantRepository tenantRepository;
    private final DatabaseConfiguration databaseConfiguration;
    public TenantDataSourceInitializer(TenantRepository tenantRepository,
                                       DatabaseConfiguration databaseConfiguration) {
        this.tenantRepository = tenantRepository;
        this.databaseConfiguration = databaseConfiguration;
    }
    @Value("${spring.datasource.username}")
    private String defaultDbUsername;

    @Value("${spring.datasource.password}")
    private String defaultDbPassword;

    @Value("${spring.datasource.url}")
    private String defaultDbUrl;

    @PostConstruct
    public void initializeActiveTenants() {
        List<Tenant> activeTenants = tenantRepository.findByStatus(TenantStatus.ACTIVE);
       // List<Tenant> tenants = tenantRepository.findAll();

        for (Tenant tenant : activeTenants) {
            String dbName = tenant.getDatabaseName();
            String jdbcUrl = "jdbc:mysql://localhost/" + dbName + "?serverTimezone=UTC&useSSL=false";
            databaseConfiguration.addTenant(dbName, jdbcUrl, defaultDbUsername, defaultDbPassword);
            System.out.println("✅ Loaded tenant from DB: " + dbName);
        }

        System.out.println("🎉 All tenants loaded at startup.");

//        for (Tenant tenant : activeTenants) {
//            try {
//                String jdbcUrl;
//                String username;
//                String password;
//
//                if (tenant.isUsesCustomDb()) {
//                    // Build JDBC URL from tenant’s host/port
//                    jdbcUrl = tenant.getJdbcUrl(tenant.getDbHost(), tenant.getDbPort());
//                    username = tenant.getDbUsername();
//                    password = tenant.getDbPassword();
//                } else {
//                    // Use default datasource info
//                    jdbcUrl = tenant.getJdbcUrlFromBaseUrl(defaultDbUrl); // Custom method explained below
//                    username = defaultDbUsername;
//                    password = defaultDbPassword;
//                }
//
//                databaseConfiguration.addTenant(tenant.getDatabaseName(), jdbcUrl, username, password);
//                System.out.println("✅ Initialized tenant: " + tenant.getDatabaseName());
//            } catch (Exception e) {
//                System.err.println("❌ Failed to initialize tenant: " + tenant.getDatabaseName());
//                e.printStackTrace();
//            }
//        }
    }
}