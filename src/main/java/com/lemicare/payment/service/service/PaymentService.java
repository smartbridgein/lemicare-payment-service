package com.lemicare.payment.service.service;

import com.cosmicdoc.common.model.PaymentOrder;
import com.cosmicdoc.common.repository.PaymentOrderRepository;
import com.cosmicdoc.common.util.IdGenerator;
import com.google.cloud.Timestamp;
import com.lemicare.payment.service.dto.request.CreateOrderRequest;
import com.lemicare.payment.service.dto.request.VerifySignatureRequest;
import com.lemicare.payment.service.dto.response.CreateOrderResponse;
import com.lemicare.payment.service.exception.ResourceNotFoundException;
import com.lemicare.payment.service.util.SignatureVerifier;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RazorpayClient razorpayClient;
    @Value("${razorpay.key-id}")
    private String keyId;
    @Value("${razorpay.key-secret}")
    private String keySecret;
    @Value("${razorpay.webhook-secret}")
    private String webhookSecret;

    private final PaymentOrderRepository paymentOrderRepository;
    // private final PaymentEventPublisher eventPublisher; // For Kafka/RabbitMQ

    public CreateOrderResponse createOrder(String orgId,String branchId, @Valid CreateOrderRequest request) {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int) (request.getAmount() * 100)); // Amount in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", request.getSourceInvoiceId());

            Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");

            PaymentOrder order = PaymentOrder.builder()
                    .orderId(IdGenerator.newId("ord"))
                    .organizationId(orgId)
                    .branchId(branchId)
                    .sourceInvoiceId(request.getSourceInvoiceId())
                    .sourceService(request.getSourceService())
                    .razorpayOrderId(razorpayOrderId)
                    .amount(request.getAmount())
                    .currency("INR")
                    .status("CREATED")
                    .createdAt(Timestamp.now())
                    .build();
            paymentOrderRepository.save(order);

            return new CreateOrderResponse(razorpayOrderId, this.keyId, request.getAmount(), "CosmicDoc Clinic");
        } catch (RazorpayException e) {
            throw new RuntimeException("Razorpay order creation failed: " + e.getMessage(), e);
        }
    }

    public boolean verifyAndProcessPayment(String orgId,String branchId, @Valid VerifySignatureRequest request) {
        try {
            // 1. Verify the signature (most critical step)
            SignatureVerifier.verifyPaymentSignature(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySignature(),
                    this.keySecret
            );

            // 2. Find our internal order record by the Razorpay Order ID.
            PaymentOrder order = paymentOrderRepository
                    .findByRazorpayOrderId(orgId,branchId,request.getRazorpayOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment order not found."));

            // 3. Update our order status (Idempotency check)
            if ("PAID".equals(order.getStatus())) {
                return true; // Already processed
            }
            order.setStatus("PAID");
            order.setRazorpayPaymentId(request.getRazorpayPaymentId());
            order.setRazorpaySignature(request.getRazorpaySignature());
            order.setUpdatedAt(Timestamp.now());
            paymentOrderRepository.save(order);

            // 4. Publish an event to notify the source service (e.g., OPD)
            // eventPublisher.publishPaymentSuccess(
            //     new PaymentSuccessEvent(orgId, order.getSourceService(), order.getSourceInvoiceId(), order.getAmount())
            // );

            return true;
        } catch (Exception e) {
            // Log the error
            return false;
        }
    }

    public void processWebhook(String payload, String signature) {
        try {
            // 1. Verify the webhook signature to ensure it's from Razorpay
            SignatureVerifier.verifyWebhookSignature(payload, signature, this.webhookSecret);

            // 2. Parse the event payload
            JSONObject event = new JSONObject(payload);
            String eventType = event.getString("event");

            if ("payment.captured".equals(eventType)) {
                JSONObject paymentEntity = event.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
                String razorpayOrderId = paymentEntity.getString("order_id");

                // Find our order and process it (same logic as verifyAndProcessPayment)
                // This is a crucial backup for cases where the user closes the browser
                // before the frontend can call our verification API.
            }
            // Handle other events like payment.failed, refund.processed, etc.

        } catch (Exception e) {
            // Log the error
        }
    }
}
