// SubscriptionRepository.java
package com.ArthurGrand.module.payment.repository;

import com.ArthurGrand.module.payment.entity.Subscription;
import com.ArthurGrand.module.payment.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByTenantIdAndStatusIn(Integer tenantId, List<SubscriptionStatus> statuses);

    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    List<Subscription> findByTenantId(Integer tenantId);

    @Query("SELECT s FROM Subscription s WHERE s.tenantId = :tenantId AND s.status IN :statuses ORDER BY s.createdAt DESC")
    Optional<Subscription> findLatestByTenantIdAndStatusIn(@Param("tenantId") Integer tenantId, @Param("statuses") List<SubscriptionStatus> statuses);

    Optional<Subscription> findByTenantIdAndStatus(Integer tenantId, SubscriptionStatus status);

    List<Subscription> findByStatus(SubscriptionStatus status);
}



