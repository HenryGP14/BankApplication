package com.devsu.hackerearth.backend.client.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartialClientDto {

    private String name;
    private String gender;
    private Integer age;
    private String address;
    private String phone;
    private String password;
    private Boolean active;

    public PartialClientDto(Boolean active) {
        this.active = active;
    }
}
