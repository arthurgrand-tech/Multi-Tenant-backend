package com.ArthurGrand.dto;

import com.ArthurGrand.common.enums.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class EmployeeViewDto {
    private Integer id;
    private String employeeId;
    private String firstname;
    private String lastname;
    private String emailId;
    private String contactNumber;
    private EmployeeStatus employeeStatus;
    private String timezone;
    private Instant createdAt;
    private Instant updatedAt;
}
