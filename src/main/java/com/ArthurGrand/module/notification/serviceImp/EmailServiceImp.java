package com.ArthurGrand.module.notification.serviceImp;

import com.ArthurGrand.dto.EmailDetailsDTO;
import com.ArthurGrand.dto.EmailTemplateBindingDTO;
import com.ArthurGrand.module.notification.service.EmailService;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailServiceImp implements EmailService {
    private final TemplateEngine templateEngine;
    private final JavaMailSender javaMailSender;
    public EmailServiceImp(TemplateEngine templateEngine,
                           JavaMailSender javaMailSender) {
        this.templateEngine = templateEngine;
        this.javaMailSender = javaMailSender;
    }
    @Override
    @Async
    public void sendEmail(EmailDetailsDTO emailDetailsDTO, EmailTemplateBindingDTO emailTemplateBindingDto,
                          String emailTemplate) {
        Context context = new Context();
        try {

            // Set template variables
            context.setVariable("contactPerson", emailTemplateBindingDto.getContactPerson()); // Added missing field
            context.setVariable("name", emailTemplateBindingDto.getName());
            context.setVariable("status", emailTemplateBindingDto.getStatus());
            context.setVariable("projects", emailTemplateBindingDto.getProjectNames());
            context.setVariable("from", emailTemplateBindingDto.getFromDate());
            context.setVariable("to", emailTemplateBindingDto.getToDate());
            context.setVariable("totalHours", emailTemplateBindingDto.getTotalHours());
            context.setVariable("organization", emailTemplateBindingDto.getOrganizationName());
            context.setVariable("navigateUrl", emailTemplateBindingDto.getPageUrl());
            context.setVariable("isApprove", emailTemplateBindingDto.isApprover());
            context.setVariable("user", emailTemplateBindingDto.getUser());
            context.setVariable("dates", emailTemplateBindingDto.getDates());
            context.setVariable("datesCount", emailTemplateBindingDto.getDatesCount());
            context.setVariable("users", emailTemplateBindingDto.getUsers());
            context.setVariable("supervisor", emailTemplateBindingDto.getSupervisor());
            context.setVariable("organizationName", emailTemplateBindingDto.getOrganizationName());
            context.setVariable("domain", emailTemplateBindingDto.getDomain());
            context.setVariable("adminEmail", emailTemplateBindingDto.getAdminEmail());

            String emailContent = templateEngine.process(emailTemplate, context);

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(emailDetailsDTO.getRecipient());
            helper.setSubject(emailDetailsDTO.getSubject());
            helper.setText(emailContent, true); // true for HTML content
            // Set a from address if not configured globally
            // helper.setFrom("noreply@yourcompany.com");
            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException("Email sending failed", e);
        } catch (Exception e) {
            throw new RuntimeException("Email processing failed", e);
        } finally {
            context.clearVariables();
        }
    }
}