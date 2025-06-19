package com.ArthurGrand.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.Instant;

@Data
public class ClientDTO {
    private Integer clientId;

    @NotBlank(message = "Client name is required")
    @Size(min = 2, max = 255, message = "Client name must be between 2 and 255 characters")
    private String clientName;

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phone;

    @Size(max = 1000, message = "Address cannot exceed 1000 characters")
    private String address;

    @Size(max = 255, message = "Website cannot exceed 255 characters")
    private String website;

    @Size(max = 255, message = "Contact person cannot exceed 255 characters")
    private String contactPerson;

    @Size(max = 100, message = "Industry cannot exceed 100 characters")
    private String industry;

    private Boolean isActive = true;

    private Instant createdAt;
    private Instant updatedAt;
}
