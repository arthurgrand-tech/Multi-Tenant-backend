package com.ArthurGrand.module.payment.aspect;

import com.ArthurGrand.module.payment.annotation.RequiresSubscription;
import com.ArthurGrand.module.payment.annotation.SubscriptionLevel;
import com.ArthurGrand.module.payment.service.SubscriptionAccessService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class SubscriptionAccessAspect {

    private final SubscriptionAccessService subscriptionAccessService;

    public SubscriptionAccessAspect(SubscriptionAccessService subscriptionAccessService) {
        this.subscriptionAccessService = subscriptionAccessService;
    }

    @Around("@annotation(requiresSubscription)")
    public Object checkSubscriptionAccess(ProceedingJoinPoint joinPoint, RequiresSubscription requiresSubscription) throws Throwable {
        SubscriptionLevel requiredLevel = requiresSubscription.level();

        boolean hasAccess = switch (requiredLevel) {
            case BASIC -> subscriptionAccessService.hasBasicAccess();
            case PREMIUM -> subscriptionAccessService.hasPremiumAccess();
            case ENTERPRISE -> subscriptionAccessService.hasEnterpriseAccess();
        };

        if (!hasAccess) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Subscription Required");
            error.put("message", "This feature requires an active " + requiredLevel.name().toLowerCase() + " subscription");
            error.put("required_level", requiredLevel.name());
            error.put("upgrade_url", "/api/v1/payment/subscription/status");

            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(error);
        }

        return joinPoint.proceed();
    }

    @Around("@within(requiresSubscription)")
    public Object checkClassLevelSubscriptionAccess(ProceedingJoinPoint joinPoint, RequiresSubscription requiresSubscription) throws Throwable {
        return checkSubscriptionAccess(joinPoint, requiresSubscription);
    }
}