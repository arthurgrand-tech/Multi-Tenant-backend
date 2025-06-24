package com.ArthurGrand.dto;

import com.ArthurGrand.module.employee.entity.Employee;
import lombok.Data;

import java.util.Set;

@Data
public class DepartmentViewDto {
    private Integer departmentId;
    private String departmentName;
    private String departmentLead;
    private Set<EmployeeViewDto> employees;
}
