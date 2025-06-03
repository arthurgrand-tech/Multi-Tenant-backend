package com.ArthurGrand.admin.tenants.serviceImp;

import com.ArthurGrand.admin.tenants.service.TenantTimeService;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TenantTimeServiceImp implements TenantTimeService {
    @Override
    public Instant now(String timeZone) {
        // Get the current time in the tenant's timezone and convert to Instant
        return ZonedDateTime.now(ZoneId.of(timeZone)).toInstant();
    }
}