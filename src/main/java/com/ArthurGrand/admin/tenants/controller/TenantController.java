package com.ArthurGrand.admin.tenants.controller;

import com.ArthurGrand.admin.dto.TenantRegisterDto;
import com.ArthurGrand.admin.dto.TenantResponseDto;
import com.ArthurGrand.admin.dto.TenantUpdateDto;
import com.ArthurGrand.admin.tenants.service.TenantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantService tenantService;
    public TenantController(TenantService tenantService){
        this.tenantService=tenantService;
    }

    @PostMapping("/createTenant")
    public ResponseEntity<?> createTenant(@RequestBody TenantRegisterDto tenantRegisterDto) {
        try {
            TenantResponseDto tenantResponseDto = tenantService.createTenant(tenantRegisterDto);
            return ResponseEntity.status(HttpStatus.OK).body(tenantResponseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
        }
    }

    @PutMapping("/{tenantId}/activate")
    public ResponseEntity<?> activateTenant(@PathVariable int tenantId) {
        try {
            TenantResponseDto response = tenantService.activateTenant(tenantId);
            return ResponseEntity.ok()
                    .body(Map.of(
                            "message", "Tenant activated successfully",
                            "tenant", response
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error: " + e.getMessage()));
        }
    }

    @PostMapping("/updateTenant")
    public ResponseEntity<?> updateTenant(@RequestBody TenantUpdateDto tenantUpdateDto){
        try {
            tenantService.updateTenant(tenantUpdateDto);
            return ResponseEntity.status(HttpStatus.OK).body("Tenant updated successful.");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
        }
    }

}
