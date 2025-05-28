package com.ArthurGrand.admin.dto;

import com.ArthurGrand.common.enums.TenantStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Data
public class TenantResponseDto  {
    private Long id;
    private String domain;
    private String companyName;
    private String databaseName;
    private String adminEmail;
    private TenantStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
