package com.lemicare.payment.service.controller;

import com.lemicare.payment.service.dto.request.CaptureRequestDto;
import com.lemicare.payment.service.dto.request.CreateOrderRequest;
import com.lemicare.payment.service.dto.request.VerifySignatureRequest;
import com.lemicare.payment.service.dto.response.CreateOrderResponse;
import com.lemicare.payment.service.dto.response.PaymentOrderStatusResponse;
import com.lemicare.payment.service.security.SecurityUtils;
import com.lemicare.payment.service.service.PaymentService;
import com.razorpay.Payment;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
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

    /**
     * Fetches a list of all payments for the organization.
     * Provides query parameters for filtering.
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<List<Payment>> listPayments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to) {
        String orgId = SecurityUtils.getOrganizationId();
        // In a real app, you would pass these filters to the service
        List<Payment> payments = paymentService.listPaymentsForOrg(orgId, from, to);
        return ResponseEntity.ok(payments);
    }

    /**
     * Fetches the details of a single payment from Razorpay.
     */
    @GetMapping("/transactions/{paymentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Payment> getPaymentDetails(@PathVariable String paymentId) {
        String orgId = SecurityUtils.getOrganizationId();
        Payment payment = paymentService.fetchPayment(orgId, paymentId);
        return ResponseEntity.ok(payment);
    }

    /**
     * Captures a payment that was previously only "authorized".
     * (Advanced feature, but good to have the endpoint).
     */
    @PostMapping("/transactions/{paymentId}/capture")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Payment> capturePayment(@PathVariable String paymentId, @RequestBody CaptureRequestDto request) {
        String orgId = SecurityUtils.getOrganizationId();
        Payment capturedPayment = paymentService.capturePayment(orgId, paymentId, request.getAmount());
        return ResponseEntity.ok(capturedPayment);
    }

    /**
     * Fetches the status of an internal payment order.
     * This is used by clients to check if a payment link is still valid or has been paid.
     */
    @GetMapping("/status/{orderId}")
    @PreAuthorize("isAuthenticated()") // Any authenticated user can check their own order status
    public ResponseEntity<PaymentOrderStatusResponse> getOrderStatus(@PathVariable String orderId) {
        String orgId = SecurityUtils.getOrganizationId();
        String branchId = SecurityUtils.getBranchId(); // Important for scoping the lookup

        PaymentOrderStatusResponse statusResponse = paymentService.getOrderStatus(orgId, branchId, orderId);
        return ResponseEntity.ok(statusResponse);
    }

    /**
     * Cancels an internal payment order to prevent it from being paid.
     * This is a status update in our system; it does not call Razorpay.
     */
    @PostMapping("/cancel/{orderId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')") // Typically an admin action
    public ResponseEntity<Map<String, String>> cancelOrder(@PathVariable String orderId) {
        String orgId = SecurityUtils.getOrganizationId();
        String branchId = SecurityUtils.getBranchId();
        paymentService.cancelOrder(orgId, branchId, orderId);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Payment order cancelled successfully."));
    }
}
