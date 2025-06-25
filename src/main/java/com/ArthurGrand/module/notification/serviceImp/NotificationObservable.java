package com.ArthurGrand.module.notification.serviceImp;

import com.ArthurGrand.module.notification.events.NotificationEvent;
import com.ArthurGrand.module.notification.service.NotificationObserver;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationObservable {
    private final List<NotificationObserver> observers;

    public NotificationObservable(List<NotificationObserver> observers) {
        this.observers = observers;
    }
    public void notifyObservers(NotificationEvent event) {
        observers.forEach(observer -> observer.handle(event));
    }
}