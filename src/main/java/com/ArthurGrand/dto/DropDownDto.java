package com.ArthurGrand.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DropDownDto {
    private Integer id;
    private String value;
    private String label;
}
