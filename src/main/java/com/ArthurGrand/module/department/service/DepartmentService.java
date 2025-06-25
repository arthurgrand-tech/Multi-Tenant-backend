package com.ArthurGrand.module.department.service;

import com.ArthurGrand.dto.DepartmentDto;
import com.ArthurGrand.dto.DepartmentSimplifyDto;
import com.ArthurGrand.dto.DepartmentViewDto;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    public int createDepartment(DepartmentDto departmentDto);
    public DepartmentViewDto getDepartment(int id);
    public Page<DepartmentViewDto> getAllDepartments(int page, int size);
    public DepartmentViewDto updateDepartment(int id, DepartmentViewDto dto);
    public boolean deleteDepartment(int id);

    void exportDepartmentDataToEmail(String recipientEmail, String format) throws MessagingException;

    Map<String, Object> getDashboardData();

    public Page<DepartmentSimplifyDto> getDepartmentsSimplified(int page, int size);
}