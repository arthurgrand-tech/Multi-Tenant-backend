package com.ArthurGrand.module.employee.serviceImp;

import com.ArthurGrand.dto.EmployeeDto;
import com.ArthurGrand.module.employee.entity.Employee;
import com.ArthurGrand.module.employee.repository.EmployeeRepository;
import com.ArthurGrand.module.employee.service.EmployeeService;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImp implements EmployeeService {

    private final EmployeeRepository employeeRepo;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImp(EmployeeRepository employeeRepo,
                              ModelMapper modelMapper,
                              PasswordEncoder passwordEncoder){
        this.employeeRepo=employeeRepo;
        this.modelMapper=modelMapper;
        this.passwordEncoder=passwordEncoder;
    }

    // REMOVED @PostConstruct method - this is now handled by MasterDatabaseService

    @Override
    public void saveEmployee(EmployeeDto employeeDto) {
        Employee employee=new Employee();
        String pswEncode=passwordEncoder.encode(employeeDto.getPassword());
        employeeDto.setPassword(pswEncode);
        modelMapper.map(employeeDto,employee);
        employeeRepo.save(employee);
    }

    @Override
    public List<EmployeeDto> getAllEmployees() {
        return employeeRepo.findAll().stream()
                .map(emp -> modelMapper.map(emp, EmployeeDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeDto getEmployeeById(Integer id) {
        Employee employee = employeeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        return modelMapper.map(employee, EmployeeDto.class);
    }
}