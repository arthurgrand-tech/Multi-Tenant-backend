package com.ArthurGrand.module.notification.serviceImp;

import com.ArthurGrand.module.notification.events.NotificationEvent;
import com.ArthurGrand.module.notification.service.AppNotificationService;
import com.ArthurGrand.module.notification.service.NotificationObserver;
import org.springframework.stereotype.Component;

@Component
public class AppNotificationObserver implements NotificationObserver {

    private final AppNotificationService appNotificationService;

    public AppNotificationObserver(AppNotificationService appNotificationService) {
        this.appNotificationService = appNotificationService;
    }

    @Override
    public void handle(NotificationEvent event) {
        appNotificationService.sendAppNotification(event.getTenantId(), event.getMessage());
    }
}