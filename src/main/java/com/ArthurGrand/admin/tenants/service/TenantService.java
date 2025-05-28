package com.ArthurGrand.admin.tenants.service;

import com.ArthurGrand.admin.dto.TenantCreateDto;
import com.ArthurGrand.admin.dto.TenantResponseDto;

import java.util.List;

public interface TenantService {
    public TenantResponseDto createTenant(TenantCreateDto tenantCreateDto) throws Exception;
    public TenantResponseDto getTenantByEmail(String email) throws Exception;
    public TenantResponseDto getTenantByDomain(String domain) throws Exception;
    public List<TenantResponseDto> getAllTenants() throws Exception;
}
