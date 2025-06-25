package com.ArthurGrand.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class TenantRegisterDto {
    @NotBlank(message = "Domain is required")
    private String domain;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Invalid email format")
    private String adminEmail;

    @NotNull(message = "usesCustomDb flag is required")
    private Boolean usesCustomDb;

    // Required only if usesCustomDb is truea
    private String dbHost;
    private Integer dbPort;
    private String dbUsername;
    private String dbPassword;

    @NotBlank(message = "Timezone is required")
    private String timezone; // e.g., "Asia/Kolkata", "America/New_York"

    @NotBlank(message = "Country is required")
    private String country;  // e.g., "IN", "US"

    // Optional profile information
    private String contactPerson;
    private String phoneNumber;
    private String address;
    private String website;
}