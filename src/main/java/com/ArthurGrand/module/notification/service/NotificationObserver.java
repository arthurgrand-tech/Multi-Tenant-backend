package com.ArthurGrand.module.notification.service;

import com.ArthurGrand.module.notification.events.NotificationEvent;

public interface NotificationObserver {
    void handle(NotificationEvent event);
}
