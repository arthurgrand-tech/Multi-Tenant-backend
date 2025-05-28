package com.ArthurGrand.common.exception;

public class TenantNotFoundException extends RuntimeException {
    public TenantNotFoundException(String email) {
        super("Tenant '" + email + "' not found.");
    }
}
