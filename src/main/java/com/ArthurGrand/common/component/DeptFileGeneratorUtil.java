package com.ArthurGrand.common.component;

import com.ArthurGrand.module.department.entity.Department;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.mail.MessagingException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Component
public class DeptFileGeneratorUtil {

    public String generateCSV(List<Department> departments) throws MessagingException {
        try {
            StringBuilder csvData = new StringBuilder();
            csvData.append("Department ID,Department Name,Department Lead\n");

            for (Department department : departments) {
                csvData.append(department.getDepartmentId()).append(",")
                        .append(department.getDepartmentName()).append(",")
                        .append(department.getDepartmentLead()).append("\n");
            }

            return csvData.toString();
        } catch (Exception e) {
            throw new MessagingException("Error generating CSV data", e);
        }
    }

    public ByteArrayResource generatePDF(List<Department> departments) throws MessagingException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A3);
            PdfWriter.getInstance(document, baos);
            document.open();

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 2, 5});

            table.addCell("Department ID");
            table.addCell("Department Name");
            table.addCell("Department Leads");

            for (Department department : departments) {
                table.addCell(String.valueOf(department.getDepartmentId()));
                table.addCell(department.getDepartmentName());
                table.addCell(department.getDepartmentLead());
            }

            document.add(table);
            document.close();

            return new ByteArrayResource(baos.toByteArray());
        } catch (Exception e) {
            throw new MessagingException("Error generating PDF file", e);
        }
    }
}
