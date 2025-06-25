package com.ArthurGrand.admin.tenants.repository;

import com.ArthurGrand.admin.tenants.entity.Tenant;
import com.ArthurGrand.common.enums.TenantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant,Integer> {
    Optional<Tenant> findByDomain(String domain);
    Optional<Tenant> findByCompanyName(String companyName);
    Optional<Tenant> findByDatabaseName(String databaseName);
    Optional<Tenant> findByAdminEmail(String adminEmail);
    List<Tenant> findByStatus(TenantStatus status);
}
