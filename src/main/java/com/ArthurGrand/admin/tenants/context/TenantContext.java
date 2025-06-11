package com.ArthurGrand.admin.tenants.context;


import com.ArthurGrand.admin.dto.UserSessionDto;

public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<UserSessionDto> currentUser = new ThreadLocal<>();

    public static void setCurrentTenant(String tenantDbName) {
        currentTenant.set(tenantDbName);
    }

    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
        currentUser.remove();
    }

    public static void setUserSession(UserSessionDto sessionDto) {
        currentUser.set(sessionDto);
    }

    public static UserSessionDto getUserSession() {
        return currentUser.get();
    }
}