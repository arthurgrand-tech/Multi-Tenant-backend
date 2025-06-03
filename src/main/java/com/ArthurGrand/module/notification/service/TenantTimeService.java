package com.ArthurGrand.module.notification.service;

import java.time.Instant;

public interface TenantTimeService {
    Instant now(String timeZone);
}
