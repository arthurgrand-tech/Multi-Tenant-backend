package com.ArthurGrand.module.employee.serviceImp;

import com.ArthurGrand.admin.tenants.context.TenantContext;
import com.ArthurGrand.dto.EmployeeDto;
import com.ArthurGrand.module.employee.entity.Employee;
import com.ArthurGrand.module.employee.repository.EmployeeRepository;
import com.ArthurGrand.module.employee.service.EmployeeService;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
    @PostConstruct
    public void saveDefaultEmployee(){
        String tenantId = "master_db"; // Your tenant database name
        try {
            TenantContext.setCurrentTenant(tenantId); // Switch to tenant
            Optional<Employee> empOpt=employeeRepo.findByEmailid("superadmin@gmail.com");
            if(!empOpt.isEmpty()){
                return;
            }
            Employee emp = new Employee();
            emp.setFirstname("Super");
            emp.setLastname("Admin");
            emp.setEmailid("superadmin@gmail.com");
            String pswEncode=passwordEncoder.encode("123");
            emp.setPassword(pswEncode);
            employeeRepo.save(emp);
        } finally {
            TenantContext.clear(); // Clear to avoid leaking tenant context
        }
    }


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
