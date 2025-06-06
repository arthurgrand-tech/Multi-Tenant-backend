package com.ArthurGrand.module.notification.service;

public interface AppNotificationService {
    void sendAppNotification(int tenantId, String message);
}
