package com.ArthurGrand.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentDto {
    private Integer departmentId;
    private String departmentName;
    private String departmentLead;
    private Set<Integer> employees;
}
