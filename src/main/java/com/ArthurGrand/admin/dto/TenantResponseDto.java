package com.ArthurGrand.admin.dto;

import com.ArthurGrand.common.enums.TenantStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Data
public class TenantResponseDto  {
    private Long tenantId;
    private String domain;
    private String companyName;
    private String adminEmail;
    private Boolean usesCustomDb;
    private String databaseName;
    private String timezone;
    private String country;
    private TenantStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
