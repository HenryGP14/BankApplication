package com.devsu.hackerearth.backend.account.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PartialAccountDto {
    private String type;
    private Double initialAmount;
    private Boolean active;
}
