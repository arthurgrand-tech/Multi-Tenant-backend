package com.ArthurGrand.admin.tenants.context;


import com.ArthurGrand.admin.dto.UserSessionDto;

public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<UserSessionDto> currentUserInfo = new ThreadLocal<>();

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

    // NEW: Store full tenant object
    public static void setUserSession(UserSessionDto userSession) {
        currentUserInfo.set(userSession);
    }

    public static UserSessionDto getUserSession() {
        return currentUserInfo.get();
    }
}
