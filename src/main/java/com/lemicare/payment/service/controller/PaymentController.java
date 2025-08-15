package com.lemicare.payment.service.controller;

import com.lemicare.payment.service.dto.request.CreateOrderRequest;
import com.lemicare.payment.service.dto.request.VerifySignatureRequest;
import com.lemicare.payment.service.dto.response.CreateOrderResponse;
import com.lemicare.payment.service.security.SecurityUtils;
import com.lemicare.payment.service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/internal/payments") // Internal-facing API
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()") // Should be protected
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createPaymentOrder(@Valid @RequestBody CreateOrderRequest request) {
        String orgId = SecurityUtils.getOrganizationId();
        String branchId = SecurityUtils.getBranchId();
        // The service returns the razorpayOrderId and your public key_id for the frontend
        return ResponseEntity.ok(paymentService.createOrder(orgId,branchId, request));
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<Map<String, String>> verifyPayment(@Valid @RequestBody VerifySignatureRequest request) {
        String orgId = SecurityUtils.getOrganizationId();
        String branchId = SecurityUtils.getBranchId();
        boolean isSuccess = paymentService.verifyAndProcessPayment(orgId,branchId,request);

        if (isSuccess) {
            return ResponseEntity.ok(Map.of("status", "success", "message", "Payment verified successfully."));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "failed", "message", "Payment verification failed."));
        }
    }
}
