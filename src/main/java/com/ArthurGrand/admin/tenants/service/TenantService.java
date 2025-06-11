package com.ArthurGrand.admin.tenants.service;

import com.ArthurGrand.admin.dto.TenantRegisterDto;
import com.ArthurGrand.admin.dto.TenantResponseDto;
import com.ArthurGrand.admin.dto.TenantUpdateDto;

import java.util.List;

public interface TenantService {
    public TenantResponseDto createTenant(TenantRegisterDto tenantRegisterDto) throws Exception;
    public TenantResponseDto activateTenant(int tenantId);
    public TenantUpdateDto updateTenant(TenantUpdateDto tenantUpdateDto);
    public TenantResponseDto getTenantByEmail(String email) throws Exception;
    public TenantResponseDto getTenantByDomain(String domain) throws Exception;
    public List<TenantResponseDto> getAllTenants() throws Exception;
}
