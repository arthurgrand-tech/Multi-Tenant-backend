package com.ArthurGrand.module.department.controller;

import com.ArthurGrand.dto.*;
import com.ArthurGrand.module.department.entity.Department;
import com.ArthurGrand.module.department.repository.DepartmentRepository;
import com.ArthurGrand.module.department.service.DepartmentService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api/v1/Department")
@CrossOrigin
public class DepartmentController {

    private final DepartmentService departmentService;
    public DepartmentController(DepartmentService departmentService){
        this.departmentService=departmentService;
    }

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Integer>> addDepartment(@RequestBody @Valid DepartmentDto departmentDto) {
        int id= departmentService.createDepartment(departmentDto);
        return ResponseEntity.ok(new ApiResponse<>("Department create successful",id));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponse<DepartmentViewDto>> getDepartmentById(@PathVariable Integer id) {
        DepartmentViewDto departmentDto = departmentService.getDepartment(id);
        if (departmentDto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Department not found", null));
        }
        return ResponseEntity.ok(new ApiResponse<>("Department retrieved successfully", departmentDto));
    }

    @GetMapping("/getAllDepartment")
    public ResponseEntity<ApiResponse<Page<DepartmentViewDto>>> getAllDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<DepartmentViewDto> departments = departmentService.getAllDepartments(page, size);
        return ResponseEntity.ok(new ApiResponse<>("Departments retrieved successfully", departments));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<DepartmentViewDto>> updateDepartment(
            @PathVariable Integer id,
            @RequestBody DepartmentViewDto dto) {

        DepartmentViewDto updated = departmentService.updateDepartment(id, dto);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Department not found", null));
        }
        return ResponseEntity.ok(new ApiResponse<>("Department updated successfully", updated));
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Integer id) {
        boolean deleted = departmentService.deleteDepartment(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Department not found", null));
        }
        return ResponseEntity.ok(new ApiResponse<>("Department deleted successfully", null));
    }


    @PostMapping(path="/export")
    public ResponseEntity<String> exportDepartmentData(@RequestBody ExportDepartmentDto requestBody) {
        String email = requestBody.getEmail();
        String format = requestBody.getFormat();

        try {
           departmentService.exportDepartmentDataToEmail(email, format);
            return ResponseEntity.ok("department data exported successfully");
        } catch (MessagingException e) {
            return ResponseEntity.badRequest().body("Failed to export department data");
        }
    }


    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> dashboardData = departmentService.getDashboardData();
        return ResponseEntity.ok(dashboardData);
    }


    @GetMapping("/simplified")
    public ResponseEntity<Map<String, Object>> getSimplifiedDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        try {
            Page<DepartmentSimplifyDto> departmentPage = departmentService.getDepartmentsSimplified(page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("departments", departmentPage.getContent());
            response.put("currentPage", departmentPage.getNumber());
            response.put("totalItems", departmentPage.getTotalElements());
            response.put("totalPages", departmentPage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error fetching departments: " + e.getMessage()));
        }
    }
}
