package com.ArthurGrand.admin.tenants.context;

public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setCurrentTenant(String tenantId) {
        System.out.println("Setting tenant context: " + tenantId);
        currentTenant.set(tenantId);
    }

    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    public static void clear() {
        System.out.println("Clearing tenant context");
        currentTenant.remove();
    }
}
