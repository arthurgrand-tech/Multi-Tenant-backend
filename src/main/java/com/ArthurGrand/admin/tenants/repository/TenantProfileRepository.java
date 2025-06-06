package com.ArthurGrand.admin.tenants.repository;

import com.ArthurGrand.admin.tenants.entity.TenantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantProfileRepository extends JpaRepository<TenantProfile,Integer> {
    Optional<TenantProfile> findByTenantId(int tenantId);
}
