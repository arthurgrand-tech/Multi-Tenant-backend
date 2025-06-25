// PaymentServiceImpl.java
package com.ArthurGrand.module.payment.service.impl;

import com.ArthurGrand.admin.tenants.context.TenantContext;
import com.ArthurGrand.admin.dto.UserSessionDto;
import com.ArthurGrand.module.payment.dto.CreateSubscriptionRequest;
import com.ArthurGrand.module.payment.dto.SubscriptionResponse;
import com.ArthurGrand.module.payment.dto.SubscriptionStatusResponse;
import com.ArthurGrand.module.payment.entity.PaymentHistory;
import com.ArthurGrand.module.payment.entity.StripeCustomer;
import com.ArthurGrand.module.payment.entity.WebhookEvent;
import com.ArthurGrand.module.payment.enums.PaymentStatus;
import com.ArthurGrand.module.payment.enums.SubscriptionStatus;
import com.ArthurGrand.module.payment.repository.PaymentHistoryRepository;
import com.ArthurGrand.module.payment.repository.StripeCustomerRepository;
import com.ArthurGrand.module.payment.repository.SubscriptionRepository;
import com.ArthurGrand.module.payment.repository.WebhookEventRepository;
import com.ArthurGrand.module.payment.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final SubscriptionRepository subscriptionRepository;
    private final StripeCustomerRepository stripeCustomerRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final WebhookEventRepository webhookEventRepository;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    public PaymentServiceImpl(SubscriptionRepository subscriptionRepository,
                              StripeCustomerRepository stripeCustomerRepository,
                              PaymentHistoryRepository paymentHistoryRepository,
                              WebhookEventRepository webhookEventRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.stripeCustomerRepository = stripeCustomerRepository;
        this.paymentHistoryRepository = paymentHistoryRepository;
        this.webhookEventRepository = webhookEventRepository;
    }

    @Override
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        try {
            UserSessionDto session = TenantContext.getUserSession();
            if (session == null) {
                throw new IllegalStateException("No tenant context found");
            }

            // Check if customer already exists
            Optional<StripeCustomer> existingCustomer = stripeCustomerRepository.findByTenantId(session.getTenantId());

            Customer stripeCustomer;
            if (existingCustomer.isPresent()) {
                stripeCustomer = Customer.retrieve(existingCustomer.get().getStripeCustomerId());
            } else {
                // Create new Stripe customer
                CustomerCreateParams customerParams = CustomerCreateParams.builder()
                        .setEmail(request.getCustomerEmail())
                        .setName(request.getCustomerName())
                        .build();

                stripeCustomer = Customer.create(customerParams);

                // Save customer to database
                StripeCustomer customer = new StripeCustomer();
                customer.setTenantId(session.getTenantId());
                customer.setStripeCustomerId(stripeCustomer.getId());
                customer.setEmail(request.getCustomerEmail());
                customer.setName(request.getCustomerName());
                stripeCustomerRepository.save(customer);
            }

            // Create subscription
            SubscriptionCreateParams subscriptionParams = SubscriptionCreateParams.builder()
                    .setCustomer(stripeCustomer.getId())
                    .addItem(SubscriptionCreateParams.Item.builder()
                            .setPrice(request.getPlanId())
                            .build())
                    .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                    .addExpand("latest_invoice.payment_intent")
                    .build();

            Subscription stripeSubscription = Subscription.create(subscriptionParams);

            // Save subscription to database
            com.ArthurGrand.module.payment.entity.Subscription subscription = new com.ArthurGrand.module.payment.entity.Subscription();
            subscription.setTenantId(session.getTenantId());
            subscription.setStripeSubscriptionId(stripeSubscription.getId());
            subscription.setStripeCustomerId(stripeCustomer.getId());
            subscription.setPlanId(request.getPlanId());
            subscription.setStatus(SubscriptionStatus.valueOf(stripeSubscription.getStatus().toUpperCase()));
            subscription.setCurrentPeriodStart(Instant.ofEpochSecond(stripeSubscription.getCurrentPeriodStart()));
            subscription.setCurrentPeriodEnd(Instant.ofEpochSecond(stripeSubscription.getCurrentPeriodEnd()));

            if (stripeSubscription.getTrialEnd() != null) {
                subscription.setTrialEnd(Instant.ofEpochSecond(stripeSubscription.getTrialEnd()));
            }

            subscriptionRepository.save(subscription);

            // Build response
            SubscriptionResponse response = new SubscriptionResponse();
            response.setSubscriptionId(stripeSubscription.getId());
            response.setStatus(stripeSubscription.getStatus());
            response.setCurrentPeriodStart(Instant.ofEpochSecond(stripeSubscription.getCurrentPeriodStart()));
            response.setCurrentPeriodEnd(Instant.ofEpochSecond(stripeSubscription.getCurrentPeriodEnd()));

            // Check if payment method is required
            if ("incomplete".equals(stripeSubscription.getStatus())) {
                Invoice invoice = (Invoice) stripeSubscription.getLatestInvoiceObject();
                if (invoice != null && invoice.getPaymentIntent() != null) {
                    PaymentIntent paymentIntent = (PaymentIntent) invoice.getPaymentIntentObject();
                    response.setRequiresPaymentMethod(true);
                    response.setClientSecret(paymentIntent.getClientSecret());
                }
            }

            return response;

        } catch (StripeException e) {
            throw new RuntimeException("Failed to create subscription: " + e.getMessage(), e);
        }
    }

    @Override
    public SubscriptionStatusResponse getSubscriptionStatus(Integer tenantId) {
        Optional<com.ArthurGrand.module.payment.entity.Subscription> subscription =
                subscriptionRepository.findLatestByTenantIdAndStatusIn(tenantId,
                        Arrays.asList(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIALING, SubscriptionStatus.PAST_DUE));

        SubscriptionStatusResponse response = new SubscriptionStatusResponse();
        if (subscription.isPresent()) {
            com.ArthurGrand.module.payment.entity.Subscription sub = subscription.get();
            response.setHasActiveSubscription(true);
            response.setStatus(sub.getStatus().name());
            response.setPlanId(sub.getPlanId());
            response.setCurrentPeriodEnd(sub.getCurrentPeriodEnd());
            response.setCancelAtPeriodEnd(sub.getCancelAtPeriodEnd());
        } else {
            response.setHasActiveSubscription(false);
            response.setStatus("NONE");
        }

        return response;
    }

    @Override
    public SubscriptionResponse cancelSubscription(Integer tenantId) {
        try {
            Optional<com.ArthurGrand.module.payment.entity.Subscription> subscriptionOpt =
                    subscriptionRepository.findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE);

            if (subscriptionOpt.isEmpty()) {
                throw new IllegalStateException("No active subscription found");
            }

            com.ArthurGrand.module.payment.entity.Subscription subscription = subscriptionOpt.get();

            // Cancel in Stripe
            Subscription stripeSubscription = Subscription.retrieve(subscription.getStripeSubscriptionId());
            stripeSubscription = stripeSubscription.cancel();

            // Update in database
            subscription.setStatus(SubscriptionStatus.CANCELED);
            subscriptionRepository.save(subscription);

            SubscriptionResponse response = new SubscriptionResponse();
            response.setSubscriptionId(stripeSubscription.getId());
            response.setStatus(stripeSubscription.getStatus());

            return response;

        } catch (StripeException e) {
            throw new RuntimeException("Failed to cancel subscription: " + e.getMessage(), e);
        }
    }

    @Override
    public SubscriptionResponse updateSubscription(Integer tenantId, String newPlanId) {
        try {
            Optional<com.ArthurGrand.module.payment.entity.Subscription> subscriptionOpt =
                    subscriptionRepository.findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE);

            if (subscriptionOpt.isEmpty()) {
                throw new IllegalStateException("No active subscription found");
            }

            com.ArthurGrand.module.payment.entity.Subscription subscription = subscriptionOpt.get();

            // Update in Stripe
            Subscription stripeSubscription = Subscription.retrieve(subscription.getStripeSubscriptionId());

            Map<String, Object> params = new HashMap<>();
            Map<String, Object> item = new HashMap<>();
            item.put("id", stripeSubscription.getItems().getData().get(0).getId());
            item.put("price", newPlanId);
            params.put("items", Arrays.asList(item));

            stripeSubscription = stripeSubscription.update(params);

            // Update in database
            subscription.setPlanId(newPlanId);
            subscriptionRepository.save(subscription);

            SubscriptionResponse response = new SubscriptionResponse();
            response.setSubscriptionId(stripeSubscription.getId());
            response.setStatus(stripeSubscription.getStatus());

            return response;

        } catch (StripeException e) {
            throw new RuntimeException("Failed to update subscription: " + e.getMessage(), e);
        }
    }

    @Override
    public void handleWebhookEvent(String payload, String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            // Check if we've already processed this event
            if (webhookEventRepository.existsByStripeEventId(event.getId())) {
                return;
            }

            // Save webhook event
            WebhookEvent webhookEvent = new WebhookEvent();
            webhookEvent.setStripeEventId(event.getId());
            webhookEvent.setEventType(event.getType());
            webhookEventRepository.save(webhookEvent);

            // Process the event
            switch (event.getType()) {
                case "invoice.payment_succeeded":
                    handlePaymentSucceeded(event);
                    break;
                case "invoice.payment_failed":
                    handlePaymentFailed(event);
                    break;
                case "customer.subscription.updated":
                    handleSubscriptionUpdated(event);
                    break;
                case "customer.subscription.deleted":
                    handleSubscriptionDeleted(event);
                    break;
                default:
                    System.out.println("Unhandled event type: " + event.getType());
            }

            // Mark as processed
            webhookEvent.setProcessed(true);
            webhookEvent.setProcessedAt(Instant.now());
            webhookEventRepository.save(webhookEvent);

        } catch (SignatureVerificationException e) {
            throw new RuntimeException("Invalid signature", e);
        } catch (Exception e) {
            System.err.println("Error processing webhook: " + e.getMessage());
            throw new RuntimeException("Webhook processing failed", e);
        }
    }

    @Override
    public boolean validateSubscriptionAccess(Integer tenantId) {
        Optional<com.ArthurGrand.module.payment.entity.Subscription> subscription =
                subscriptionRepository.findLatestByTenantIdAndStatusIn(tenantId,
                        Arrays.asList(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIALING));

        if (subscription.isPresent()) {
            return subscription.get().getCurrentPeriodEnd().isAfter(Instant.now());
        }

        return false;
    }

    private void handlePaymentSucceeded(Event event) {
        // Implementation for payment succeeded
        Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
        if (invoice != null && invoice.getSubscription() != null) {
            savePaymentHistory(invoice, PaymentStatus.SUCCEEDED);
        }
    }

    private void handlePaymentFailed(Event event) {
        // Implementation for payment failed
        Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
        if (invoice != null && invoice.getSubscription() != null) {
            savePaymentHistory(invoice, PaymentStatus.FAILED);
        }
    }

    private void handleSubscriptionUpdated(Event event) {
        // Implementation for subscription updated
        Subscription stripeSubscription = (Subscription) event.getDataObjectDeserializer().getObject().orElse(null);
        if (stripeSubscription != null) {
            updateSubscriptionStatus(stripeSubscription);
        }
    }

    private void handleSubscriptionDeleted(Event event) {
        // Implementation for subscription deleted
        Subscription stripeSubscription = (Subscription) event.getDataObjectDeserializer().getObject().orElse(null);
        if (stripeSubscription != null) {
            updateSubscriptionStatus(stripeSubscription);
        }
    }

    private void savePaymentHistory(Invoice invoice, PaymentStatus status) {
        Optional<com.ArthurGrand.module.payment.entity.Subscription> subscriptionOpt =
                subscriptionRepository.findByStripeSubscriptionId(invoice.getSubscription());

        if (subscriptionOpt.isPresent()) {
            PaymentHistory payment = new PaymentHistory();
            payment.setTenantId(subscriptionOpt.get().getTenantId());
            payment.setStripeInvoiceId(invoice.getId());
            payment.setSubscriptionId(subscriptionOpt.get().getId());
            payment.setAmount(BigDecimal.valueOf(invoice.getAmountPaid() / 100.0));
            payment.setCurrency(invoice.getCurrency().toUpperCase());
            payment.setStatus(status);

            paymentHistoryRepository.save(payment);
        }
    }

    private void updateSubscriptionStatus(Subscription stripeSubscription) {
        Optional<com.ArthurGrand.module.payment.entity.Subscription> subscriptionOpt =
                subscriptionRepository.findByStripeSubscriptionId(stripeSubscription.getId());

        if (subscriptionOpt.isPresent()) {
            com.ArthurGrand.module.payment.entity.Subscription subscription = subscriptionOpt.get();
            subscription.setStatus(SubscriptionStatus.valueOf(stripeSubscription.getStatus().toUpperCase()));
            subscription.setCurrentPeriodStart(Instant.ofEpochSecond(stripeSubscription.getCurrentPeriodStart()));
            subscription.setCurrentPeriodEnd(Instant.ofEpochSecond(stripeSubscription.getCurrentPeriodEnd()));
            subscription.setCancelAtPeriodEnd(stripeSubscription.getCancelAtPeriodEnd());

            subscriptionRepository.save(subscription);
        }
    }
}