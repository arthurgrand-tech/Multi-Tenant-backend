package com.ArthurGrand.admin.tenants.service;

import java.time.Instant;

public interface TenantTimeService {
    public Instant now(String timeZone);
    public Instant localNow();

    public Instant weekStart(Instant now);
    public Instant weekEnd(Instant now);
}
