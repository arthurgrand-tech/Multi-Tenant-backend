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
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class DatabaseConfiguration {

    @Value("${spring.datasource.url}")
    private String masterUrl;

    @Value("${spring.datasource.username}")
    private String masterUsername;

    @Value("${spring.datasource.password}")
    private String masterPassword;

    private MultiTenantDataSource multiTenantDataSource;
    private final Map<String, DataSource> dataSourceCache = new ConcurrentHashMap<>();

    @Bean
    @Primary
    public DataSource dataSource() {
        Map<Object, Object> targetDataSources = new HashMap<>();

        // Master database
        DataSource masterDataSource = createDataSource(masterUrl, masterUsername, masterPassword);
        targetDataSources.put("master", masterDataSource);
        dataSourceCache.put("master", masterDataSource);

        multiTenantDataSource = new MultiTenantDataSource();
        multiTenantDataSource.setTargetDataSources(targetDataSources);
        multiTenantDataSource.setDefaultTargetDataSource(masterDataSource);
        multiTenantDataSource.afterPropertiesSet();

        System.out.println("✅ Database configuration initialized with master datasource");
        return multiTenantDataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    public DataSource createDataSource(String url, String username, String password) {
        try {
            System.out.println("🔧 Creating datasource for URL: " + url);

            // Check if datasource already exists in cache
            String cacheKey = url + ":" + username;
            if (dataSourceCache.containsKey(cacheKey)) {
                System.out.println("♻️ Reusing cached datasource for: " + url);
                return dataSourceCache.get(cacheKey);
            }

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            // Connection pool settings
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000); // 10 minutes
            config.setMaxLifetime(1800000); // 30 minutes
            config.setLeakDetectionThreshold(60000); // 1 minute

            // Connection validation
            config.setConnectionTestQuery("SELECT 1");
            config.setValidationTimeout(5000);

            // Additional MySQL specific settings
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");

            HikariDataSource dataSource = new HikariDataSource(config);
            dataSourceCache.put(cacheKey, dataSource);

            System.out.println("✅ Successfully created datasource for: " + url);
            return dataSource;

        } catch (Exception e) {
            System.err.println("❌ Failed to create datasource for URL: " + url);
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create datasource for " + url, e);
        }
    }

    public synchronized void addTenant(String databaseName, String url, String username, String password) {
        System.out.println("🔧 Registering tenant DB: " + databaseName);

        if (multiTenantDataSource == null) {
            throw new IllegalStateException("MultiTenantDataSource is not initialized");
        }

        try {
            // Check if tenant datasource already exists
            if (dataSourceCache.containsKey(databaseName)) {
                System.out.println("♻️ Tenant datasource already exists: " + databaseName);
                return;
            }

            Map<Object, Object> targetDataSources = new HashMap<>();

            // Keep existing datasources
            Map<Object, DataSource> existing = multiTenantDataSource.getResolvedDataSources();
            if (existing != null) {
                targetDataSources.putAll(existing);
            }

            // Add new tenant datasource
            DataSource newDataSource = createDataSource(url, username, password);
            targetDataSources.put(databaseName, newDataSource);
            dataSourceCache.put(databaseName, newDataSource);

            // Update the routing datasource
            multiTenantDataSource.setTargetDataSources(targetDataSources);
            multiTenantDataSource.afterPropertiesSet();

            System.out.println("✅ Successfully registered tenant: " + databaseName);
            System.out.println("✅ Total registered tenants: " + targetDataSources.keySet());

        } catch (Exception e) {
            System.err.println("❌ Failed to add tenant datasource: " + databaseName);
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to add tenant datasource: " + databaseName, e);
        }
    }

    public void removeTenant(String databaseName) {
        System.out.println("🗑️ Removing tenant DB: " + databaseName);

        try {
            // Close and remove from cache
            DataSource dataSource = dataSourceCache.remove(databaseName);
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
            }

            // Update routing datasource
            Map<Object, Object> targetDataSources = new HashMap<>();
            Map<Object, DataSource> existing = multiTenantDataSource.getResolvedDataSources();
            if (existing != null) {
                existing.entrySet().stream()
                        .filter(entry -> !databaseName.equals(entry.getKey()))
                        .forEach(entry -> targetDataSources.put(entry.getKey(), entry.getValue()));
            }

            multiTenantDataSource.setTargetDataSources(targetDataSources);
            multiTenantDataSource.afterPropertiesSet();

            System.out.println("✅ Successfully removed tenant: " + databaseName);

        } catch (Exception e) {
            System.err.println("❌ Failed to remove tenant datasource: " + databaseName);
            e.printStackTrace();
        }
    }

    public boolean tenantExists(String databaseName) {
        return dataSourceCache.containsKey(databaseName);
    }

    public DataSource getTenantDataSource(String databaseName) {
        return dataSourceCache.get(databaseName);
    }
}