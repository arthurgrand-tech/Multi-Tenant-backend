package com.ArthurGrand.admin.tenants.serviceImp;

import com.ArthurGrand.admin.tenants.service.TenantTimeService;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;

@Service
public class TenantTimeServiceImp implements TenantTimeService {

    /**
     * Get the current Instant based on given time zone.
     */
    @Override
    public Instant now(String timeZone) {
        return ZonedDateTime.now(ZoneId.of(timeZone)).toInstant();
    }

    /**
     * Get the current Instant in local system default zone.
     */
    @Override
    public Instant localNow() {
        return Instant.now();
    }

    /**
     * Parse a string date based on time zone and format, and return LocalDateTime in that zone.
     *
     * @param dateTimeStr the date time string, e.g., "03/06/2025 14:30:00"
     * @param timeZone the IANA time zone ID, e.g., "Asia/Kolkata"
     * @param format the input format, e.g., "dd/MM/yyyy HH:mm:ss"
     * @return LocalDateTime in the given time zone
     */
    public LocalDateTime parseToLocalDateTime(String dateTimeStr, String timeZone, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, formatter);
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of(timeZone));
        return zonedDateTime.toLocalDateTime(); // same as input but respects time zone offset
    }
}
