package com.ArthurGrand.module.notification.serviceImp;

import com.ArthurGrand.dto.EmailDetailsDTO;
import com.ArthurGrand.dto.EmailTemplateBindingDTO;
import com.ArthurGrand.module.notification.events.NotificationEvent;
import com.ArthurGrand.module.notification.service.AppNotificationService;
import com.ArthurGrand.module.notification.service.EmailService;
import com.ArthurGrand.module.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class NotificationServiceImp implements NotificationService {

    private final EmailService emailService;
    private final AppNotificationService appNotificationService;

    public NotificationServiceImp(EmailService emailService,
                                  AppNotificationService appNotificationService) {
        this.emailService = emailService;
        this.appNotificationService = appNotificationService;
    }

    @Override
    @Async("notificationExecutor")
    public CompletableFuture<Void> sendNotification(String email, int tenantId, String subject,
                                                    String message, String emailTemplate,
                                                    EmailTemplateBindingDTO binding) {
        try {
            System.out.println("Starting async notification for email: {}"+ email);

            EmailDetailsDTO emailDetails = new EmailDetailsDTO();
            emailDetails.setRecipient(email);
            emailDetails.setSubject(subject);
            emailDetails.setMsgBody(message);
            emailDetails.setEmailTemplate(emailTemplate);

            // Send email
            emailService.sendEmail(emailDetails, binding, emailTemplate);
            System.out.println("Email sent successfully to: {}"+ email);

            // Send in-app notification
            appNotificationService.sendAppNotification(tenantId, message);
            System.out.println("In-app notification sent for tenant: {}"+ tenantId);

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            System.out.println("Error sending notification to {}: {}"+ email+ e.getMessage()+ e);
            return CompletableFuture.failedFuture(e);
        }
    }



    @EventListener
    @Async("notificationExecutor")
    public void onNotificationEvent(NotificationEvent event) {
        System.out.println("Handling NotificationEvent asynchronously for tenant: {}"+ event.getTenantId());

        try {
            CompletableFuture<Void> future = sendNotification(
                    event.getEmail(),
                    event.getTenantId(),
                    event.getSubject(),
                    event.getMessage(),
                    event.getEmailTemplate(),
                    event.getTemplateBinding()
            );

            // Optional: wait with timeout (you can remove this line for fire-and-forget)
            future.get(30, TimeUnit.SECONDS);

        } catch (Exception e) {
            System.out.println("Failed to process notification event: {}"+ e.getMessage()+ e);
        }
    }
}