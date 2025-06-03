package com.ArthurGrand.module.notification.service;

import com.ArthurGrand.dto.EmailDetailsDTO;
import com.ArthurGrand.dto.EmailTemplateBindingDTO;

public interface EmailService {
    void sendEmail(EmailDetailsDTO emailDetailsDTO , EmailTemplateBindingDTO emailTemplateBindingDto,
                   String emailTemplate);
}
