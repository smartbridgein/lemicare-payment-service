package com.lemicare.payment.service.controller;

import com.lemicare.payment.service.dto.request.CreateRefundRequestDto;
import com.lemicare.payment.service.security.SecurityUtils;
import com.lemicare.payment.service.service.RefundService;
import com.razorpay.Refund;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/refunds")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class RefundController {

    private final RefundService refundService;

    /**
     * Initiates a refund for a successful payment.
     */
    @PostMapping("/")
    public ResponseEntity<Refund> initiateRefund(@Valid @RequestBody CreateRefundRequestDto request) {
        String orgId = SecurityUtils.getOrganizationId();
        String userId = SecurityUtils.getUserId();
        Refund refund = refundService.createRefund(orgId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(refund);
    }

    /**
     * Fetches the status of a specific refund.
     */
    @GetMapping("/{refundId}")
    public ResponseEntity<Refund> getRefundStatus(@PathVariable String refundId) {
        String orgId = SecurityUtils.getOrganizationId();
        Refund refund = refundService.fetchRefund(orgId, refundId);
        return ResponseEntity.ok(refund);
    }
}
