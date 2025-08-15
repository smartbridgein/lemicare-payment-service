package com.lemicare.payment.service.controller;

import com.lemicare.payment.service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/razorpay")
@RequiredArgsConstructor
public class WebhookController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Event-Id") String eventId,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        // The service handles signature verification and processing
        paymentService.processWebhook(payload, signature);

        return ResponseEntity.ok("Webhook received.");
    }
}
