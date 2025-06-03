package com.ArthurGrand.module.notification.service;

public interface AppNotificationService {
    void sendAppNotification(Long tenantId, String message);
}
