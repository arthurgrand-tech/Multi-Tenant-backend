// PaymentHistoryRepository.java
package com.ArthurGrand.module.payment.repository;

import com.ArthurGrand.module.payment.entity.PaymentHistory;
import com.ArthurGrand.module.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    List<PaymentHistory> findByTenantIdOrderByCreatedAtDesc(Integer tenantId);

    List<PaymentHistory> findBySubscriptionIdOrderByCreatedAtDesc(Long subscriptionId);

    Optional<PaymentHistory> findByStripePaymentIntentId(String stripePaymentIntentId);

    Optional<PaymentHistory> findByStripeInvoiceId(String stripeInvoiceId);

    List<PaymentHistory> findByTenantIdAndStatus(Integer tenantId, PaymentStatus status);

    @Query("SELECT p FROM PaymentHistory p WHERE p.tenantId = :tenantId AND p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    List<PaymentHistory> findByTenantIdAndDateRange(@Param("tenantId") Integer tenantId,
                                                    @Param("startDate") Instant startDate,
                                                    @Param("endDate") Instant endDate);
}

