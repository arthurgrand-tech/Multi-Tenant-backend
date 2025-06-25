
// SubscriptionResponse.java
package com.ArthurGrand.module.payment.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class SubscriptionResponse {

    private String subscriptionId;

    private String status;

    private String planId;

    private Instant currentPeriodStart;

    private Instant currentPeriodEnd;

    private Boolean cancelAtPeriodEnd;

    private Instant trialEnd;

    private Boolean requiresPaymentMethod = false;

    private String clientSecret;

    private String message;
}
