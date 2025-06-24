package com.ArthurGrand.module.payment.service;

import com.ArthurGrand.admin.tenants.context.TenantContext;
import com.ArthurGrand.admin.dto.UserSessionDto;
import com.ArthurGrand.module.payment.entity.Subscription;
import com.ArthurGrand.module.payment.enums.SubscriptionStatus;
import com.ArthurGrand.module.payment.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

@Service
public class SubscriptionAccessService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionAccessService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public boolean hasActiveSubscription() {
        UserSessionDto session = TenantContext.getUserSession();
        if (session == null) {
            return false;
        }

        Optional<Subscription> subscription = subscriptionRepository.findByTenantIdAndStatusIn(
                session.getTenantId(),
                Arrays.asList(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIALING)
        );

        return subscription.isPresent() && !isSubscriptionExpired(subscription.get());
    }

    public boolean hasPremiumAccess() {
        UserSessionDto session = TenantContext.getUserSession();
        if (session == null) {
            return false;
        }

        Optional<Subscription> subscription = subscriptionRepository.findByTenantIdAndStatusIn(
                session.getTenantId(),
                Arrays.asList(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIALING)
        );

        if (subscription.isPresent() && !isSubscriptionExpired(subscription.get())) {
            // Check if it's a premium plan
            String planId = subscription.get().getPlanId();
            return planId != null && (planId.contains("premium") || planId.contains("pro"));
        }

        return false;
    }

    public SubscriptionStatus getSubscriptionStatus() {
        UserSessionDto session = TenantContext.getUserSession();
        if (session == null) {
            return null;
        }

        Optional<Subscription> subscription = subscriptionRepository.findByTenantIdAndStatusIn(
                session.getTenantId(),
                Arrays.asList(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIALING, SubscriptionStatus.PAST_DUE)
        );

        return subscription.map(Subscription::getStatus).orElse(null);
    }

    private boolean isSubscriptionExpired(Subscription subscription) {
        return subscription.getCurrentPeriodEnd().isBefore(Instant.now());
    }
}