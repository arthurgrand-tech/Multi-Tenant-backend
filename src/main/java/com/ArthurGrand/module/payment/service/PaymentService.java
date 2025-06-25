// PaymentService.java
package com.ArthurGrand.module.payment.service;

import com.ArthurGrand.module.payment.dto.CreateSubscriptionRequest;
import com.ArthurGrand.module.payment.dto.SubscriptionResponse;
import com.ArthurGrand.module.payment.dto.SubscriptionStatusResponse;
import com.stripe.model.Subscription;

public interface PaymentService {

    SubscriptionResponse createSubscription(CreateSubscriptionRequest request);

    SubscriptionStatusResponse getSubscriptionStatus(Integer tenantId);

    SubscriptionResponse cancelSubscription(Integer tenantId);

    SubscriptionResponse updateSubscription(Integer tenantId, String newPlanId);

    void handleWebhookEvent(String payload, String sigHeader);

    boolean validateSubscriptionAccess(Integer tenantId);
}

