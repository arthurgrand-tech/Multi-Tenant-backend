package com.ArthurGrand.module.notification.serviceImp;

import com.ArthurGrand.module.notification.service.AppNotificationService;
import org.springframework.stereotype.Service;

@Service
public class AppNotificationServiceImp implements AppNotificationService {
    @Override
    public void sendAppNotification(Long tenantId, String message) {
        // Log or save to DB
        System.out.println("APP NOTIFICATION: [" + tenantId + "] " + message);
    }
}
