package com.ArthurGrand.module.payment.controller;

import com.ArthurGrand.admin.dto.UserSessionDto;
import com.ArthurGrand.admin.tenants.context.TenantContext;
import com.ArthurGrand.dto.ApiResponse;
import com.ArthurGrand.module.payment.annotation.RequiresSubscription;
import com.ArthurGrand.module.payment.annotation.SubscriptionLevel;
import com.ArthurGrand.module.payment.dto.CreateSubscriptionRequest;
import com.ArthurGrand.module.payment.dto.PaymentMethodUpdateRequest;
import com.ArthurGrand.module.payment.dto.SubscriptionResponse;
import com.ArthurGrand.module.payment.dto.SubscriptionStatusResponse;
import com.ArthurGrand.module.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment")
@Tag(name = "Payment Management", description = "Payment and subscription management endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/subscription/create")
    @Operation(summary = "Create a new subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("Validation errors", null));
        }

        try {
            SubscriptionResponse response = paymentService.createSubscription(request);
            return ResponseEntity.ok(new ApiResponse<>("Subscription created successfully", response));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to create subscription: " + e.getMessage(), null));
        }
    }

    @GetMapping("/subscription/status")
    @Operation(summary = "Get current subscription status")
    public ResponseEntity<ApiResponse<SubscriptionStatusResponse>> getSubscriptionStatus() {
        try {
            UserSessionDto session = TenantContext.getUserSession();
            if (session == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>("No tenant context found", null));
            }

            SubscriptionStatusResponse response = paymentService.getSubscriptionStatus(session.getTenantId());
            return ResponseEntity.ok(new ApiResponse<>("Subscription status retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to get subscription status: " + e.getMessage(), null));
        }
    }

    @PostMapping("/subscription/cancel")
    @Operation(summary = "Cancel current subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancelSubscription() {
        try {
            UserSessionDto session = TenantContext.getUserSession();
            if (session == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>("No tenant context found", null));
            }

            SubscriptionResponse response = paymentService.cancelSubscription(session.getTenantId());
            return ResponseEntity.ok(new ApiResponse<>("Subscription cancelled successfully", response));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to cancel subscription: " + e.getMessage(), null));
        }
    }

    @PostMapping("/subscription/update/{planId}")
    @Operation(summary = "Update subscription plan")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> updateSubscription(@PathVariable String planId) {
        try {
            UserSessionDto session = TenantContext.getUserSession();
            if (session == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>("No tenant context found", null));
            }

            SubscriptionResponse response = paymentService.updateSubscription(session.getTenantId(), planId);
            return ResponseEntity.ok(new ApiResponse<>("Subscription updated successfully", response));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to update subscription: " + e.getMessage(), null));
        }
    }

    @PostMapping("/webhook")
    @Operation(summary = "Handle Stripe webhooks")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            paymentService.handleWebhookEvent(payload, sigHeader);
            return ResponseEntity.ok("Webhook handled successfully");
        } catch (Exception e) {
            System.err.println("Webhook error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook handling failed");
        }
    }

    @GetMapping("/subscription/validate")
    @Operation(summary = "Validate subscription access")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> validateSubscriptionAccess() {
        try {
            UserSessionDto session = TenantContext.getUserSession();
            if (session == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>("No tenant context found", null));
            }

            boolean hasAccess = paymentService.validateSubscriptionAccess(session.getTenantId());
            Map<String, Boolean> result = new HashMap<>();
            result.put("hasAccess", hasAccess);

            return ResponseEntity.ok(new ApiResponse<>("Subscription validation completed", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to validate subscription: " + e.getMessage(), null));
        }
    }

    // Example of using subscription level annotation
    @GetMapping("/premium-feature")
    @RequiresSubscription(level = SubscriptionLevel.PREMIUM)
    @Operation(summary = "Access premium feature")
    public ResponseEntity<ApiResponse<String>> accessPremiumFeature() {
        return ResponseEntity.ok(new ApiResponse<>("Premium feature accessed", "Welcome to premium!"));
    }

    @GetMapping("/basic-feature")
    @RequiresSubscription(level = SubscriptionLevel.BASIC)
    @Operation(summary = "Access basic feature")
    public ResponseEntity<ApiResponse<String>> accessBasicFeature() {
        return ResponseEntity.ok(new ApiResponse<>("Basic feature accessed", "Welcome to basic tier!"));
    }
}