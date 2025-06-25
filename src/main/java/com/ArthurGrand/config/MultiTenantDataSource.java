package com.ArthurGrand.config;

import com.ArthurGrand.admin.tenants.context.TenantContext;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class MultiTenantDataSource extends AbstractRoutingDataSource {

    private Map<Object, DataSource> resolvedDataSources = new HashMap<>();

    @Override
    protected Object determineCurrentLookupKey() {
        String tenant = TenantContext.getCurrentTenant();

        // Default to master if no tenant context is set
        String lookupKey = tenant != null ? tenant : "master";

        System.out.println("🔄 Routing to database: " + lookupKey);
        System.out.println("🔄 Available datasources: " + resolvedDataSources.keySet());

        return lookupKey;
    }

    public Map<Object, DataSource> getResolvedDataSources() {
        return new HashMap<>(this.resolvedDataSources);
    }

    @Override
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        super.setTargetDataSources(targetDataSources);

        // Cache the resolved datasources
        this.resolvedDataSources.clear();
        for (Map.Entry<Object, Object> entry : targetDataSources.entrySet()) {
            this.resolvedDataSources.put(entry.getKey(), (DataSource) entry.getValue());
        }

        System.out.println("🔄 Updated datasource routing table with keys: " + this.resolvedDataSources.keySet());
    }

    @Override
    protected DataSource determineTargetDataSource() {
        Object lookupKey = determineCurrentLookupKey();
        DataSource dataSource = this.resolvedDataSources.get(lookupKey);

        if (dataSource == null) {
            System.err.println("❌ No datasource found for key: " + lookupKey);
            System.err.println("❌ Available keys: " + this.resolvedDataSources.keySet());

            // Fallback to master datasource
            dataSource = this.resolvedDataSources.get("master");
            if (dataSource == null) {
                throw new IllegalStateException("No master datasource configured");
            }
            System.out.println("⚠️ Falling back to master datasource");
        }

        return dataSource;
    }
}