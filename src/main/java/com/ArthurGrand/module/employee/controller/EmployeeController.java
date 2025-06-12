package com.ArthurGrand.module.employee.controller;


import com.ArthurGrand.admin.dto.UserSessionDto;
import com.ArthurGrand.admin.tenants.context.TenantContext;
import com.ArthurGrand.dto.ApiResponse;
import com.ArthurGrand.dto.EmployeeDto;
import com.ArthurGrand.module.employee.entity.Employee;
import com.ArthurGrand.module.employee.repository.EmployeeRepository;
import com.ArthurGrand.module.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/employee")
public class EmployeeController {
    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepo;
    private final DataSource dataSource; // 🔁 Now Spring can inject the MultiTenantDataSource

    public EmployeeController(EmployeeService employeeService,
    EmployeeRepository employeeRepo, DataSource dataSource){
        this.employeeService=employeeService;
        this.employeeRepo=employeeRepo;
        this.dataSource=dataSource;
    }

    @GetMapping("/which-db")
    public String whichDb() throws SQLException {
        String tenant = TenantContext.getCurrentTenant();
        String catalog = dataSource.getConnection().getCatalog();
        System.out.println("🧪 Tenant: " + tenant + " | DB used: " + catalog);
        return catalog;
    }

    @PostMapping("/saveEmployee")
    public ResponseEntity<ApiResponse<?>> saveEmployee(@Valid @RequestBody EmployeeDto employeeDto, BindingResult result) {
        if (result.hasErrors()) {
            String errors = result.getAllErrors()
                    .stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.joining(", "));

            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", Instant.now());
            error.put("status", HttpStatus.BAD_REQUEST.value());
            error.put("error", "Validation Error");
            error.put("message", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("Validation Error",error));
        }
        boolean emailExists= employeeRepo.existsByEmailid(employeeDto.getEmailid());
       if(emailExists){
           return ResponseEntity.status(HttpStatus.CONFLICT)
                   .body(new ApiResponse<>("Email Already exists",null));
       }
        employeeService.saveEmployee(employeeDto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Employee saved successfully.",null));
    }
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<EmployeeDto>>> getAllEmployees() {
        List<EmployeeDto> employees = employeeService.getAllEmployees();
        UserSessionDto us= TenantContext.getUserSession();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Employee fetch successful",employees));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<EmployeeDto>> getEmployeeById(@PathVariable Integer id) {
        Optional<Employee> employee=employeeRepo.findById(id);
        if(employee.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Employee not found",null));
        }
        EmployeeDto employeeDto = employeeService.getEmployeeById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Employee fetch successful",employeeDto));
    }
}
