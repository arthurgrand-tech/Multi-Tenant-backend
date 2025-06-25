
// PaymentMethodUpdateRequest.java
package com.ArthurGrand.module.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentMethodUpdateRequest {

    @NotBlank(message = "Payment method ID is required")
    private String paymentMethodId;

    private Boolean setAsDefault = true;
}