package com.lemicare.payment.service.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CaptureRequestDto {
    @NotNull
    @Positive
    private Double amount;
}
