package com.ArthurGrand.admin.tenants.repository;

import com.ArthurGrand.admin.tenants.entity.TenantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantProfileRepository extends JpaRepository<TenantProfile,Long> {
}
