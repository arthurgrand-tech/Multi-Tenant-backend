package com.ArthurGrand.module.notification.serviceImp;

import com.ArthurGrand.dto.EmailDetailsDto;
import com.ArthurGrand.module.notification.events.NotificationEvent;
import com.ArthurGrand.module.notification.service.EmailService;
import com.ArthurGrand.module.notification.service.NotificationObserver;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationObserver implements NotificationObserver {

    private final EmailService emailService;

    public EmailNotificationObserver(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void handle(NotificationEvent event) {
        EmailDetailsDto details = new EmailDetailsDto();
        details.setRecipient(event.getEmail());
        details.setSubject(event.getSubject());
        details.setMsgBody(event.getMessage());
        details.setEmailTemplate(event.getEmailTemplate());

        emailService.sendEmail(details, event.getTemplateBinding(), event.getEmailTemplate());
    }
}

