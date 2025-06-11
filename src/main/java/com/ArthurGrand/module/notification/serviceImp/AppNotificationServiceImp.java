package com.ArthurGrand.module.notification.serviceImp;

import com.ArthurGrand.module.notification.service.AppNotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AppNotificationServiceImp implements AppNotificationService {
    @Override
    @Async
    public void sendAppNotification(int tenantId, String message) {
        // Log or save to DB
        System.out.println("APP NOTIFICATION: [" + tenantId + "] " + message);
    }
}
