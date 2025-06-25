package com.ArthurGrand.dto;

import com.ArthurGrand.common.enums.EmployeeStatus;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class EmployeeDto {

    private Integer id;

    private String employeeId;

    @NotBlank(message = "First name is required")
    private String firstname;

    private String lastname;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String emailId;

    @NotBlank(message = "Password is required")
    private String password;

    private String contactNumber;

    @NotNull(message = "Employee status is required")
    private EmployeeStatus employeeStatus;

}