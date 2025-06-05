package com.ArthurGrand.admin.dto;

import com.ArthurGrand.common.enums.TenantStatus;
import lombok.Data;

@Data
public class TenantSession {
    private Long id;
    private String domain;
    private String timezone;
    private TenantStatus status;
}
