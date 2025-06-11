package com.ArthurGrand.admin.dto;

import lombok.Data;

@Data
public class UserSessionDto {
    private int tenantId;
    private int employeeId;
    private String domain;
    private String timezone;
}
