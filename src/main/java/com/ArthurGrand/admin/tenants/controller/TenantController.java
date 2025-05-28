package com.ArthurGrand.admin.tenants.controller;

import com.ArthurGrand.admin.dto.TenantCreateDto;
import com.ArthurGrand.admin.dto.TenantResponseDto;
import com.ArthurGrand.admin.tenants.service.TenantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantService tenantService;
    public TenantController(TenantService tenantService){
        this.tenantService=tenantService;
    }

    @PostMapping("/createTenant")
    public ResponseEntity<?> createTenant(@RequestBody TenantCreateDto tenantCreateDto) {
        try {
            TenantResponseDto tenantResponseDto = tenantService.createTenant(tenantCreateDto);
            return ResponseEntity.status(HttpStatus.OK).body(tenantResponseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
        }
    }

}
