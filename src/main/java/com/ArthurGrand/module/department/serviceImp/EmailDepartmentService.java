package com.ArthurGrand.module.department.serviceImp;

import com.ArthurGrand.common.component.DeptFileGeneratorUtil;
import com.ArthurGrand.module.department.entity.Department;
import com.ArthurGrand.module.notification.service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailDepartmentService {

    @Value("${spring.mail.username}")
    private String fromEmail; // Injected from application.properties

    private final EmailService emailService;
    private final DeptFileGeneratorUtil deptFileGeneratorUtil;

    public EmailDepartmentService(EmailService emailService,
                                  DeptFileGeneratorUtil deptFileGeneratorUtil) {
        this.emailService = emailService;
        this.deptFileGeneratorUtil = deptFileGeneratorUtil;
    }

    public void sendEmailWithCSV(String recipientEmail, List<Department> departments) throws MessagingException {
        String csvData = deptFileGeneratorUtil.generateCSV(departments);
        ByteArrayResource resource = new ByteArrayResource(csvData.getBytes());

        emailService.sendEmailWithAttachment(
                fromEmail,
                recipientEmail,
                "Department Data CSV",
                "Please find the attached CSV file.",
                "Department_data.csv",
                resource
        );
    }

    public void sendEmailWithPDF(String recipientEmail, List<Department> departments) throws MessagingException {
        ByteArrayResource pdfAttachment = deptFileGeneratorUtil.generatePDF(departments);

        emailService.sendEmailWithAttachment(
                fromEmail,
                recipientEmail,
                "Department Data PDF",
                "Please find the attached PDF file.",
                "Department_data.pdf",
                pdfAttachment
        );
    }
}
