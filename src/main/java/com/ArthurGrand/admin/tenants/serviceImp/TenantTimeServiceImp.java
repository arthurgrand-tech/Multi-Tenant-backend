package com.ArthurGrand.admin.tenants.serviceImp;

import com.ArthurGrand.admin.tenants.service.TenantTimeService;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

@Service
public class TenantTimeServiceImp implements TenantTimeService {

    @Override
    public Instant now(String timeZone) {
        return ZonedDateTime.now(ZoneId.of(timeZone)).toInstant();
    }

    @Override
    public Instant localNow() {
        return Instant.now();
    }

    /**
     * Calculate the start of the week (Monday at 00:00) for the given Instant.
     */
    @Override
    public Instant weekStart(Instant now) {
        // Use system default zone or customize per tenant if needed
        ZonedDateTime zonedDateTime = now.atZone(ZoneId.systemDefault());
        ZonedDateTime startOfWeek = zonedDateTime
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toLocalDate()
                .atStartOfDay(zonedDateTime.getZone());
        return startOfWeek.toInstant();
    }

    /**
     * Calculate the end of the week (Sunday at 23:59:59.999) for the given Instant.
     */
    @Override
    public Instant weekEnd(Instant now) {
        ZonedDateTime zonedDateTime = now.atZone(ZoneId.systemDefault());
        ZonedDateTime endOfWeek = zonedDateTime
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .toLocalDate()
                .atTime(LocalTime.MAX)  // 23:59:59.999999999
                .atZone(zonedDateTime.getZone());
        return endOfWeek.toInstant();
    }

    public LocalDateTime parseToLocalDateTime(String dateTimeStr, String timeZone, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, formatter);
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of(timeZone));
        return zonedDateTime.toLocalDateTime();
    }
}
