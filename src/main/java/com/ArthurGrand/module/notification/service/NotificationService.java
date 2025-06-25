package com.ArthurGrand.module.notification.service;

import com.ArthurGrand.dto.EmailTemplateBindingDto;
import com.ArthurGrand.module.notification.events.NotificationEvent;

import java.util.concurrent.CompletableFuture;

public interface NotificationService {
    // Updated method signature to return CompletableFuture<Void>
    CompletableFuture<Void> sendNotification(String email, int tenantId, String subject,
                                             String message, String emailTemplate,
                                             EmailTemplateBindingDto binding);

    // Add the event listener method to the interface
    void onNotificationEvent(NotificationEvent event);
}
