package com.ArthurGrand.module.employee.service;


import com.ArthurGrand.dto.EmployeeDto;

import java.util.List;

public interface EmployeeService{
    public void saveEmployee(EmployeeDto employee);
    List<EmployeeDto> getAllEmployees();
    EmployeeDto getEmployeeById(Integer id);

}
