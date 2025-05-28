package com.ArthurGrand.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class TenantCreateDto  {
    @NotBlank
    private String domain;

    @NotBlank
    private String companyName;

    @NotBlank
    private String databaseName;

    @Email
    @NotBlank
    private String adminEmail;
}
