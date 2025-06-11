package com.ArthurGrand.admin.dto;

import com.ArthurGrand.common.enums.TenantStatus;
import lombok.Data;

@Data
public class TenantUpdateDto {

    private int tenantId;

    private String domain;

    private String companyName;

    private boolean usesCustomDb;

    // Optional: required only if usesCustomDb is true
    private String dbHost;
    private Integer dbPort;
    private String dbUsername;
    private String dbPassword;

    private String adminEmail;

    private TenantStatus status;

    private String timezone;

    private String country;
}