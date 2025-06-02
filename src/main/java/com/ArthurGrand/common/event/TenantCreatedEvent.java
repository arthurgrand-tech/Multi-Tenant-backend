package com.ArthurGrand.common.event;

import com.ArthurGrand.admin.tenants.entity.Tenant;
import org.springframework.context.ApplicationEvent;

public class TenantCreatedEvent extends ApplicationEvent {
    private final Tenant tenant;

    public TenantCreatedEvent(Object source, Tenant tenant) {
        super(source);
        this.tenant = tenant;
    }

    public Tenant getTenant() {
        return tenant;
    }
}