package com.ArthurGrand.module.employee.serviceImp;

import com.ArthurGrand.admin.tenants.context.TenantContext;
import com.ArthurGrand.common.enums.EmployeeStatus;
import com.ArthurGrand.dto.EmployeeDto;
import com.ArthurGrand.dto.EmployeeViewDto;
import com.ArthurGrand.module.employee.entity.Employee;
import com.ArthurGrand.module.employee.repository.EmployeeRepository;
import com.ArthurGrand.module.employee.service.EmployeeService;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
            Optional<Employee> empOpt=employeeRepo.findByEmailId("superadmin@gmail.com");
            if(!empOpt.isEmpty()){
                return;
            }
            Employee emp = new Employee();
            emp.setEmployeeId("AGT000");
            emp.setFirstName("Super");
            emp.setLastName("Admin");
            emp.setEmailId("superadmin@gmail.com");
            String pswEncode=passwordEncoder.encode("123qwe");
            emp.setPassword(pswEncode);
            emp.setEmployeeStatus(EmployeeStatus.ACTIVE);
            emp.setContactNumber("1234567890");
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
    public Page<EmployeeViewDto> getAllEmployees(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Employee> employeePage=employeeRepo.findAll(pageable);
        return employeePage.map(emp->{
            EmployeeViewDto empView=new EmployeeViewDto();
            modelMapper.map(emp,empView);
            return empView;
        });
    }

    @Override
    public EmployeeViewDto getEmployeeById(Integer id) {
        Employee employee = employeeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        return modelMapper.map(employee, EmployeeViewDto.class);
    }

    @Override
    public String updateEmployee(Integer id, EmployeeDto employeeDto) {
        Employee existing = employeeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + id));
        modelMapper.map(employeeDto,existing);
        employeeRepo.save(existing);
        return "Employee updated with ID: " + id;
    }

    @Override
    public void deleteEmployee(Integer id) {
        if (!employeeRepo.existsById(id)) {
            throw new RuntimeException("Employee not found with ID: " + id);
        }
        employeeRepo.deleteById(id);
    }

}
