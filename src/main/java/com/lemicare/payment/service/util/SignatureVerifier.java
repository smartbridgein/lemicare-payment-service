package com.lemicare.payment.service.util;

import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;

public final class SignatureVerifier {
    private SignatureVerifier() {}

    public static void verifyPaymentSignature(String orderId, String paymentId, String signature, String secret) throws RazorpayException {
        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", orderId);
        options.put("razorpay_payment_id", paymentId);
        options.put("razorpay_signature", signature);

        if (!Utils.verifyPaymentSignature(options, secret)) {
            throw new RazorpayException("Payment signature verification failed.");
        }
    }

    public static void verifyWebhookSignature(String payload, String signature, String secret) throws RazorpayException {
        if (!Utils.verifyWebhookSignature(payload, signature, secret)) {
            throw new RazorpayException("Webhook signature verification failed.");
        }
    }
}
