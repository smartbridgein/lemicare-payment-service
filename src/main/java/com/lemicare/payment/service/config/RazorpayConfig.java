package com.lemicare.payment.service.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class responsible for initializing the Razorpay client.
 * This class reads the API keys from the application properties and creates
 * the RazorpayClient object as a managed Spring Bean.
 */
@Configuration
public class RazorpayConfig {

    // Injects the key-id value from your application.properties file
    @Value("${razorpay.key-id}")
    private String keyId;

    // Injects the key-secret value from your application.properties file
    @Value("${razorpay.key-secret}")
    private String keySecret;

    /**
     * Creates and configures the RazorpayClient as a Spring Bean.
     * <p>
     * By annotating this method with @Bean, Spring will execute it during startup
     * and register the returned RazorpayClient object in the application context.
     * This makes it available for dependency injection into other services, like PaymentService.
     *
     * @return An initialized RazorpayClient instance.
     * @throws RazorpayException if the keys are invalid.
     */
    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        return new RazorpayClient(keyId, keySecret);
    }
}
