// StripeCustomerRepository.java
package com.ArthurGrand.module.payment.repository;

import com.ArthurGrand.module.payment.entity.StripeCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StripeCustomerRepository extends JpaRepository<StripeCustomer, Long> {

    Optional<StripeCustomer> findByTenantId(Integer tenantId);

    Optional<StripeCustomer> findByStripeCustomerId(String stripeCustomerId);

    Optional<StripeCustomer> findByEmail(String email);

    boolean existsByTenantId(Integer tenantId);
}