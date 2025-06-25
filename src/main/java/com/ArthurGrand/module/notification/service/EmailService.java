package com.ArthurGrand.module.notification.service;

import com.ArthurGrand.dto.EmailDetailsDto;
import com.ArthurGrand.dto.EmailTemplateBindingDto;
import jakarta.mail.MessagingException;
import org.springframework.core.io.ByteArrayResource;

public interface EmailService {
    void sendEmail(EmailDetailsDto emailDetailsDTO , EmailTemplateBindingDto emailTemplateBindingDto,
                   String emailTemplate);
    public void sendEmailWithAttachment(
            String from,
            String to,
            String subject,
            String body,
            String attachmentName,
            ByteArrayResource attachmentData
    ) throws MessagingException;
}
