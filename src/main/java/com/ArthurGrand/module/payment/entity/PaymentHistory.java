package com.ArthurGrand.module.payment.entity;

import com.ArthurGrand.module.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.math.BigDecimal;


@Entity
@Data
@Table(name = "payment_history")
public class PaymentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @Column(name = "stripe_invoice_id")
    private String stripeInvoiceId;

    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "payment_method_type", length = 50)
    private String paymentMethodType;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }
}

