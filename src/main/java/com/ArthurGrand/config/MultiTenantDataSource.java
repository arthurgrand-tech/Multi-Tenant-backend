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
        System.out.println("Routing to tenant: " + tenant);
        System.out.println("Current lookup key: " + TenantContext.getCurrentTenant());

        return tenant != null ? tenant : "master";
    }

    public Map<Object, DataSource> getResolvedDataSources() {
        return this.resolvedDataSources;
    }

    @Override
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        super.setTargetDataSources(targetDataSources);

        // Cache the resolved datasources
        for (Map.Entry<Object, Object> entry : targetDataSources.entrySet()) {
            resolvedDataSources.put(entry.getKey(), (DataSource) entry.getValue());
        }
    }
}

