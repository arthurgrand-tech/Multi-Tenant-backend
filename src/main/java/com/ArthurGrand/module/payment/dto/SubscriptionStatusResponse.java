// SubscriptionStatusResponse.java
package com.ArthurGrand.module.payment.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class SubscriptionStatusResponse {

    private Boolean hasActiveSubscription;

    private String status;

    private String planId;

    private Instant currentPeriodEnd;

    private Boolean cancelAtPeriodEnd;

    private Instant trialEnd;

    private Boolean isPastDue;

    private String nextInvoiceDate;
}
