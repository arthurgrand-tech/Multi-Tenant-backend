package com.ArthurGrand.dto;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private String message;
    private T response;

    public ApiResponse(String message,T response){
        this.message=message;
        this.response=response;
    }
}
