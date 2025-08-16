package com.lemicare.payment.service.service;

import com.lemicare.payment.service.exception.ResourceNotFoundException;
import com.razorpay.Refund;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.lemicare.payment.service.dto.request.CreateRefundRequestDto;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RazorpayClient razorpayClient;
    // Inject repositories for your internal refund records

    public Refund createRefund(String orgId, String userId, CreateRefundRequestDto request) {
        try {
            JSONObject refundRequest = new JSONObject();
            // Refund amount can be partial
            refundRequest.put("amount", (int) (request.getAmount() * 100));
            refundRequest.put("speed", request.isImmediate() ? "optimum" : "normal");

            JSONObject notes = new JSONObject();
            notes.put("reason", request.getReason());
            notes.put("initiated_by", userId);
            refundRequest.put("notes", notes);

            // Initiate the refund against the original paymentId
            Refund refund = razorpayClient.payments.refund(request.getPaymentId(), refundRequest);

            // TODO: Create a 'refunds' record in your own database to track this.

            return refund;
        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to initiate refund for payment " + request.getPaymentId(), e);
        }
    }

    public Refund fetchRefund(String orgId, String refundId) {
        try {
            // This is a direct API call to get the latest status of a refund.
            return razorpayClient.refunds.fetch(refundId);
        } catch (RazorpayException e) {
            throw new ResourceNotFoundException("Refund with ID " + refundId + " not found on Razorpay.", e);
        }
    }
}
