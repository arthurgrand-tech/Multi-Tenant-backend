package com.ArthurGrand.dto;

import lombok.Data;

@Data
public class EmailDetailsDto {
    private String recipient;
    private String msgBody;
    private String subject;
    private String attachment;
    public String emailTemplate;
}
