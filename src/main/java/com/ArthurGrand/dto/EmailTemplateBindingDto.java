package com.ArthurGrand.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class EmailTemplateBindingDto {
    private String name;
    private String status;
    private String supervisor;
    private String user;
    private String fromDate;
    private String toDate;
    private BigDecimal totalHours;
    private String organizationName;
    private List<String> projectNames;
    private String pageUrl;
    private boolean isApprover;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private  List<Instant> dates;
    private int datesCount;
    private List<String> users;
    private String projectName;
    private Instant creationDate;
    private String domain;
    private String adminEmail;
    private String contactPerson;


}