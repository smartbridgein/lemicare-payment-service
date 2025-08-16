package com.lemicare.payment.service.dto.response;

import com.cosmicdoc.common.model.PaymentOrder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentOrderStatusResponse {
    private String orderId;
    private String status; // "CREATED", "PAID", "FAILED", "CANCELLED"
    private double amount;

    public static PaymentOrderStatusResponse from(PaymentOrder order) {
        return PaymentOrderStatusResponse.builder()
                .orderId(order.getOrderId())
                .status(order.getStatus())
                .amount(order.getAmount())
                .build();
    }
}
