package com.ArthurGrand.admin.tenants.serviceImp;

import com.ArthurGrand.admin.tenants.entity.Tenant;
import com.ArthurGrand.admin.tenants.repository.TenantRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TenantCacheService {
    private final TenantRepository tenantRepository;

    public TenantCacheService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Cacheable(value = "tenants", key = "#domain")
    public Optional<Tenant> getTenantByDomain(String domain) {
        System.out.println("💾 Loading tenant from DB: " + domain);
        return tenantRepository.findByDomain(domain); // no exception
    }

    @CacheEvict(value = "tenants", key = "#domain")
    public void evictTenant(String domain) {
        System.out.println("🧹 Evicted cache for domain: " + domain);
    }

    @CachePut(value = "tenants", key = "#tenant.domain")
    public Tenant updateTenantCache(Tenant tenant) {
        System.out.println("🔁 Updated cache for domain: " + tenant.getDomain());
        return tenant;
    }
}
