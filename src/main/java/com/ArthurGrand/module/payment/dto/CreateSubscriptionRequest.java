// CreateSubscriptionRequest.java
package com.ArthurGrand.module.payment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSubscriptionRequest {

    @NotBlank(message = "Plan ID is required")
    private String planId;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    private String paymentMethodId;

    private Boolean enableTrial = false;

    private Integer trialDays;
}

