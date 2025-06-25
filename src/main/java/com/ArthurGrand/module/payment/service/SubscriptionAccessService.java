package com.ArthurGrand.module.payment.service;

import com.ArthurGrand.admin.tenants.context.TenantContext;
import com.ArthurGrand.admin.dto.UserSessionDto;
import com.ArthurGrand.module.payment.entity.Subscription;
import com.ArthurGrand.module.payment.enums.SubscriptionStatus;
import com.ArthurGrand.module.payment.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
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

        List<SubscriptionStatus> activeStatuses = Arrays.asList(
                SubscriptionStatus.ACTIVE,
                SubscriptionStatus.TRIALING
        );

        Optional<Subscription> subscription = subscriptionRepository
                .findByTenantIdAndStatusIn(session.getTenantId(), activeStatuses);

        return subscription.isPresent() && !isSubscriptionExpired(subscription.get());
    }

    public boolean hasPremiumAccess() {
        UserSessionDto session = TenantContext.getUserSession();
        if (session == null) {
            return false;
        }

        List<SubscriptionStatus> activeStatuses = Arrays.asList(
                SubscriptionStatus.ACTIVE,
                SubscriptionStatus.TRIALING
        );

        Optional<Subscription> subscription = subscriptionRepository
                .findByTenantIdAndStatusIn(session.getTenantId(), activeStatuses);

        if (subscription.isPresent() && !isSubscriptionExpired(subscription.get())) {
            // Check if it's a premium plan
            String planId = subscription.get().getPlanId();
            return planId != null && (
                    planId.toLowerCase().contains("premium") ||
                            planId.toLowerCase().contains("pro") ||
                            planId.toLowerCase().contains("enterprise")
            );
        }

        return false;
    }

    public boolean hasBasicAccess() {
        return hasActiveSubscription();
    }

    public boolean hasEnterpriseAccess() {
        UserSessionDto session = TenantContext.getUserSession();
        if (session == null) {
            return false;
        }

        List<SubscriptionStatus> activeStatuses = Arrays.asList(
                SubscriptionStatus.ACTIVE,
                SubscriptionStatus.TRIALING
        );

        Optional<Subscription> subscription = subscriptionRepository
                .findByTenantIdAndStatusIn(session.getTenantId(), activeStatuses);

        if (subscription.isPresent() && !isSubscriptionExpired(subscription.get())) {
            String planId = subscription.get().getPlanId();
            return planId != null && planId.toLowerCase().contains("enterprise");
        }

        return false;
    }

    public SubscriptionStatus getSubscriptionStatus() {
        UserSessionDto session = TenantContext.getUserSession();
        if (session == null) {
            return null;
        }

        List<SubscriptionStatus> allStatuses = Arrays.asList(
                SubscriptionStatus.ACTIVE,
                SubscriptionStatus.TRIALING,
                SubscriptionStatus.PAST_DUE,
                SubscriptionStatus.CANCELED,
                SubscriptionStatus.INCOMPLETE
        );

        Optional<Subscription> subscription = subscriptionRepository
                .findByTenantIdAndStatusIn(session.getTenantId(), allStatuses);

        return subscription.map(Subscription::getStatus).orElse(null);
    }

    public Subscription getCurrentSubscription() {
        UserSessionDto session = TenantContext.getUserSession();
        if (session == null) {
            return null;
        }

        List<SubscriptionStatus> activeStatuses = Arrays.asList(
                SubscriptionStatus.ACTIVE,
                SubscriptionStatus.TRIALING
        );

        return subscriptionRepository
                .findByTenantIdAndStatusIn(session.getTenantId(), activeStatuses)
                .orElse(null);
    }

    public boolean isSubscriptionExpired() {
        Subscription subscription = getCurrentSubscription();
        return subscription == null || isSubscriptionExpired(subscription);
    }

    private boolean isSubscriptionExpired(Subscription subscription) {
        if (subscription == null) {
            return true;
        }

        // Check if current period has ended
        boolean periodExpired = subscription.getCurrentPeriodEnd().isBefore(Instant.now());

        // If it's a trial, check trial end date
        if (subscription.getStatus() == SubscriptionStatus.TRIALING &&
                subscription.getTrialEnd() != null) {
            return subscription.getTrialEnd().isBefore(Instant.now());
        }

        return periodExpired;
    }

    public long getDaysUntilExpiration() {
        Subscription subscription = getCurrentSubscription();
        if (subscription == null) {
            return 0;
        }

        Instant expirationDate = subscription.getCurrentPeriodEnd();
        if (subscription.getStatus() == SubscriptionStatus.TRIALING &&
                subscription.getTrialEnd() != null) {
            expirationDate = subscription.getTrialEnd();
        }

        long secondsUntilExpiration = expirationDate.getEpochSecond() - Instant.now().getEpochSecond();
        return secondsUntilExpiration / (24 * 60 * 60); // Convert to days
    }
}