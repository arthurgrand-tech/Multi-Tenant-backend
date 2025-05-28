package com.ArthurGrand.common.component;

import com.ArthurGrand.admin.tenants.entity.Tenant;
import com.ArthurGrand.admin.tenants.repository.TenantRepository;
import com.ArthurGrand.config.DatabaseConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.List;

@Component
public class TenantDataSourceInitializer {

    private final TenantRepository tenantRepository;
    private final DatabaseConfiguration databaseConfiguration;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    public TenantDataSourceInitializer(
            TenantRepository tenantRepository,
            DatabaseConfiguration databaseConfiguration
    ) {
        this.tenantRepository = tenantRepository;
        this.databaseConfiguration = databaseConfiguration;
    }

    @PostConstruct
    public void loadTenants() {
        List<Tenant> tenants = tenantRepository.findAll();

        for (Tenant tenant : tenants) {
            String dbName = tenant.getDatabaseName();
            String jdbcUrl = "jdbc:mysql://localhost/" + dbName + "?serverTimezone=UTC&useSSL=false";
            databaseConfiguration.addTenant(dbName, jdbcUrl, dbUsername, dbPassword);
            System.out.println("✅ Loaded tenant from DB: " + dbName);
        }

        System.out.println("🎉 All tenants loaded at startup.");
    }
}

