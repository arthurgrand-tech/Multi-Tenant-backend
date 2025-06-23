package com.ArthurGrand.module.department.service;

import com.ArthurGrand.dto.DepartmentDto;
import com.ArthurGrand.dto.DepartmentSimplifyDto;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    String addDepartment(DepartmentDto departmentDTO);

    List<DepartmentDto> getAllDepartment();

    Integer updateDepartment(DepartmentDto departmentDTO);

    boolean deleteDepartment(int id);

    void exportDepartmentDataToEmail(String recipientEmail, String format) throws MessagingException;

    Map<String, Object> getDashboardData();

    public Page<DepartmentSimplifyDto> getDepartmentsSimplified(int page, int size);
}