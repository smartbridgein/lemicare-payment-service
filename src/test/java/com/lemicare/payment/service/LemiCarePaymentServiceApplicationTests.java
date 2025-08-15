package com.lemicare.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemicare.payment.service.controller.PaymentController;
import com.lemicare.payment.service.dto.request.CreateOrderRequest;
import com.lemicare.payment.service.dto.request.VerifySignatureRequest;
import com.lemicare.payment.service.dto.response.CreateOrderResponse;
import com.lemicare.payment.service.filter.TenantFilter;
import com.lemicare.payment.service.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType; // <-- CORRECTED IMPORT
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // This is correct
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post; // <-- CORRECTED IMPORT
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath; // <-- CORRECTED IMPORT
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class) // Use only @WebMvcTest for controller unit tests
@Import(TenantFilter.class)
class PaymentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private PaymentService mockPaymentService;

	private CreateOrderRequest validOrderRequest;
	private VerifySignatureRequest validVerifyRequest;

	@BeforeEach
	void setUp() {
		validOrderRequest = new CreateOrderRequest();
		validOrderRequest.setAmount(1500.00);
		validOrderRequest.setCurrency("INR");
		validOrderRequest.setSourceInvoiceId("inv_12345");
		validOrderRequest.setSourceService("OPD");

		validVerifyRequest = new VerifySignatureRequest();
		validVerifyRequest.setRazorpayOrderId("order_ABC123");
		validVerifyRequest.setRazorpayPaymentId("pay_DEF456");
		validVerifyRequest.setRazorpaySignature("valid_signature");
	}

	// --- Tests for the createPaymentOrder Endpoint ---

	@Test
	@WithMockUser
	void createPaymentOrder_whenValidRequest_shouldReturn200Ok() throws Exception {
		CreateOrderResponse mockResponse = new CreateOrderResponse("order_ABC123", "rzp_test_key", 1500.00, "CosmicDoc");
		when(mockPaymentService.createOrder(any(), any(), any(CreateOrderRequest.class))).thenReturn(mockResponse);

		mockMvc.perform(post("/api/internal/payments/create-order")
						.with(csrf())
						.with(jwt().jwt(jwt -> jwt
								.claim("organizationId", "test-org-123")
								.claim("branchId", "test-branch-456")
								.subject("test-user-789") // for userId
						))
						.contentType(MediaType.APPLICATION_JSON) // This now works
						.content(objectMapper.writeValueAsString(validOrderRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.razorpayOrderId").value("order_ABC123")) // This now works
				.andExpect(jsonPath("$.razorpayKeyId").value("rzp_test_key"));
	}

	@Test
	void createPaymentOrder_whenUnauthenticated_shouldReturn401Unauthorized() throws Exception {
		mockMvc.perform(post("/api/internal/payments/create-order")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validOrderRequest)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser
	void createPaymentOrder_whenInvalidBody_shouldReturn400BadRequest() throws Exception {
		CreateOrderRequest invalidRequest = new CreateOrderRequest();
		invalidRequest.setSourceInvoiceId("inv_123");

		mockMvc.perform(post("/api/internal/payments/create-order")
						.with(csrf())
						.with(jwt().jwt(jwt -> jwt
						.claim("organizationId", "test-org-123")
						.claim("branchId", "test-branch-456")
						.subject("test-user-789") // for userId
				))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidRequest)))
				.andExpect(status().isBadRequest());
	}

	// --- Tests for the verifyPayment Endpoint ---

	@Test
	@WithMockUser
	void verifyPayment_whenSignatureIsValid_shouldReturn200Ok() throws Exception {
		when(mockPaymentService.verifyAndProcessPayment(any(), any(), any(VerifySignatureRequest.class))).thenReturn(true);

		mockMvc.perform(post("/api/internal/payments/verify-payment")
						.with(csrf())
						.with(jwt().jwt(jwt -> jwt
								.claim("organizationId", "test-org-123")
								.claim("branchId", "test-branch-456")
								.subject("test-user-789") // for userId
						))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validVerifyRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("success"));
	}

	@Test
	@WithMockUser
	void verifyPayment_whenSignatureIsInvalid_shouldReturn400BadRequest() throws Exception {
		when(mockPaymentService.verifyAndProcessPayment(any(), any(), any(VerifySignatureRequest.class))).thenReturn(false);

		mockMvc.perform(post("/api/internal/payments/verify-payment")
						.with(csrf())
						.with(jwt().jwt(jwt -> jwt
								.claim("organizationId", "test-org-123")
								.claim("branchId", "test-branch-456")
								.subject("test-user-789") // for userId
						))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validVerifyRequest)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value("failed"));
	}
}