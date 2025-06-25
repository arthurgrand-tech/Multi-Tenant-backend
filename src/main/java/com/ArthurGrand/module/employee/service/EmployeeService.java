package com.ArthurGrand.module.employee.service;


import com.ArthurGrand.dto.EmployeeDto;
import com.ArthurGrand.dto.EmployeeViewDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface EmployeeService{
    public void saveEmployee(EmployeeDto employee);
    public Page<EmployeeViewDto> getAllEmployees(int page, int size);
    EmployeeViewDto getEmployeeById(Integer id);
    String updateEmployee(Integer id, EmployeeDto employeeDto);
    void deleteEmployee(Integer id);

}
