package com.devsu.hackerearth.backend.client.model.dto;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BasicResponse<T> {
    
    private int code; 
    private String status;
    private String message; 
    private T data;

    public static <T> BasicResponse<T> of (HttpStatus httpStatus, String message, T data){
        return new BasicResponse<T>(httpStatus.value(), httpStatus.name(), message, data);
    }
}
