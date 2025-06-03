package com.ArthurGrand.module.notification.serviceImp;

import com.ArthurGrand.admin.tenants.serviceImp.TenantServiceImp;
import com.ArthurGrand.module.notification.service.TenantTimeService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class TenantTimeServiceImp implements TenantTimeService {

    @Override
    public Instant now(String timeZone) {
        // Get the current time in the tenant's timezone and convert to Instant
        return ZonedDateTime.now(ZoneId.of(timeZone)).toInstant();
    }
}
