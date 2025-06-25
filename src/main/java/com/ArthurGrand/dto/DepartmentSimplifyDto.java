package com.ArthurGrand.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentSimplifyDto {
    private Integer departmentId;
    private String departmentName;
    private String departmentLead;
    private Set<Map<String, Object>> employeeNames = new HashSet<>();
}
