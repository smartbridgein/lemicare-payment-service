package com.lemicare.payment.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateRefundRequestDto {
    @NotBlank
    private String paymentId; // The original razorpay_payment_id to refund
    @NotNull
    @Positive
    private Double amount; // The amount to refund
    @NotBlank
    private String reason;
    private boolean immediate = false; // For instant refunds
}
