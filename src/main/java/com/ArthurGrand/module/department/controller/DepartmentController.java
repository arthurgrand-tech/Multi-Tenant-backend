package com.ArthurGrand.module.department.controller;

import com.ArthurGrand.dto.DepartmentDto;
import com.ArthurGrand.dto.DepartmentSimplifyDto;
import com.ArthurGrand.dto.ExportDepartmentDto;
import com.ArthurGrand.module.department.service.DepartmentService;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/Department")
@CrossOrigin
public class DepartmentController {

    private final DepartmentService departmentService;
    public DepartmentController(DepartmentService departmentService){
        this.departmentService=departmentService;
    }

    @PostMapping(path = "/save")
    private String saveDepartment(@RequestBody DepartmentDto departmentDTO)
    {
        String id = departmentService.addDepartment(departmentDTO);
        return id;
    }



    @GetMapping(path = "/getAllDepartment")
    public List<DepartmentDto> getAllDepartment() {
        List<DepartmentDto> allDepartment = departmentService.getAllDepartment();
        return allDepartment;
    }


    @PostMapping(path = "/update")
    public Integer updateDepartment(@RequestBody DepartmentDto departmentDTO)
    {
        Integer id = departmentService.updateDepartment(departmentDTO);
        return id;
    }


    @DeleteMapping(path = "/delete/{id}")
    public String deleteDepartment(@PathVariable(value = "id") int id)
    {
        boolean deleteDepartment = departmentService.deleteDepartment(id);
        return "deleted";
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
