package com.ArthurGrand.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmployeeDto {
    private int id;
    private String employeeId;
    @NotBlank(message = "First name is required")
    private String firstname;

    private String lastname;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String emailId;

    @NotBlank(message = "Password is required")
    private String password;
}
