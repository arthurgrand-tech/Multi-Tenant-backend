package com.ArthurGrand.module.notification.events;

import com.ArthurGrand.dto.EmailCategory;
import com.ArthurGrand.dto.EmailTemplateBindingDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class NotificationEvent {
    private final int tenantId;
    private final String email;
    private final String subject;
    private final String message;
    private final String emailTemplate;
    private final EmailTemplateBindingDTO templateBinding;
    private final EmailCategory emailCategory;
}