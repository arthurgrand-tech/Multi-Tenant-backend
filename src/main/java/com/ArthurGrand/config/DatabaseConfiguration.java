package com.ArthurGrand.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DatabaseConfiguration {

    @Value("${spring.datasource.url}")
    private String masterUrl;

    @Value("${spring.datasource.username}")
    private String masterUsername;

    @Value("${spring.datasource.password}")
    private String masterPassword;

    private MultiTenantDataSource multiTenantDataSource;

    @Bean
    @Primary
    public DataSource dataSource() {
        Map<Object, Object> targetDataSources = new HashMap<>();

        // Master database
        DataSource masterDataSource = createDataSource(masterUrl, masterUsername, masterPassword);
        targetDataSources.put("master", masterDataSource);

        multiTenantDataSource = new MultiTenantDataSource();
        multiTenantDataSource.setTargetDataSources(targetDataSources);
        multiTenantDataSource.setDefaultTargetDataSource(masterDataSource);
        multiTenantDataSource.afterPropertiesSet();

        return multiTenantDataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    public DataSource createDataSource(String url, String username, String password) {
        try {
            System.out.println("🔧 Creating datasource for URL: " + url);
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000); // 10 minutes
            config.setMaxLifetime(1800000); // 30 minutes
            config.setLeakDetectionThreshold(60000); // 1 minute

            return new HikariDataSource(config);
        } catch (Exception e) {
            System.err.println("❌ Failed to create datasource for URL: " + url);
            System.err.println("❌ Error: " + e.getMessage());
            throw new RuntimeException("Failed to create datasource for " + url, e);
        }
    }

    public void addTenant(String databaseName, String url, String username, String password) {
        System.out.println("Registering tenant DB: " + databaseName);

        if (multiTenantDataSource == null) {
            throw new IllegalStateException("MultiTenantDataSource is not initialized");
        }

        try {
            Map<Object, Object> targetDataSources = new HashMap<>();

            // Keep existing first
            Map<Object, DataSource> existing = multiTenantDataSource.getResolvedDataSources();
            targetDataSources.putAll(existing);

            // Add new tenant
            DataSource newDataSource = createDataSource(url, username, password);
            targetDataSources.put(databaseName, newDataSource);

            multiTenantDataSource.setTargetDataSources(targetDataSources);
            multiTenantDataSource.afterPropertiesSet();

            System.out.println("✅ Registered tenants: " + targetDataSources.keySet());
        } catch (Exception e) {
            System.err.println("❌ Failed to add tenant datasource: " + databaseName);
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to add tenant datasource: " + databaseName, e);
        }
    }
}