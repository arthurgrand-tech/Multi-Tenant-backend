package com.ArthurGrand.module.employee.controller;


import com.ArthurGrand.admin.dto.UserSessionDto;
import com.ArthurGrand.admin.tenants.context.TenantContext;
import com.ArthurGrand.dto.ApiResponse;
import com.ArthurGrand.dto.EmployeeDto;
import com.ArthurGrand.dto.EmployeeViewDto;
import com.ArthurGrand.module.employee.entity.Employee;
import com.ArthurGrand.module.employee.repository.EmployeeRepository;
import com.ArthurGrand.module.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
        boolean emailExists= employeeRepo.existsByEmailId(employeeDto.getEmailId());
       if(emailExists){
           return ResponseEntity.status(HttpStatus.CONFLICT)
                   .body(new ApiResponse<>("Email Already exists",null));
       }
        employeeService.saveEmployee(employeeDto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Employee saved successfully.",null));
    }
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<EmployeeViewDto>>> getAllEmployees(@RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "10") int size) {
        Page<EmployeeViewDto> employees = employeeService.getAllEmployees(page,size);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Employee fetch successful",employees));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponse<EmployeeViewDto>> getEmployeeById(@PathVariable Integer id) {
        Optional<Employee> employee=employeeRepo.findById(id);
        if(employee.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Employee not found",null));
        }
        EmployeeViewDto employeeDto = employeeService.getEmployeeById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Employee fetch successful",employeeDto));
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteEmployee(@PathVariable Integer id) {
        Optional<Employee> employee=employeeRepo.findById(id);
        if(employee.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Employee not found",null));
        }
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(new ApiResponse<>("Employee deleted with ID: " + id,null));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateEmployee(@PathVariable Integer id, @RequestBody @Valid EmployeeDto employeeDto) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, employeeDto));
    }
}
