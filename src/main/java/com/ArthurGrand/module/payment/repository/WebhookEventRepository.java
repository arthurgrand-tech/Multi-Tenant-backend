// WebhookEventRepository.java
package com.ArthurGrand.module.payment.repository;

import com.ArthurGrand.module.payment.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    Optional<WebhookEvent> findByStripeEventId(String stripeEventId);

    List<WebhookEvent> findByProcessedFalseOrderByCreatedAtAsc();

    List<WebhookEvent> findByEventTypeAndProcessedFalse(String eventType);

    boolean existsByStripeEventId(String stripeEventId);
}