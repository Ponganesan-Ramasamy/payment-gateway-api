package com.brillio.paymentgateway;

import com.brillio.paymentgateway.dto.PaymentRequest;
import com.brillio.paymentgateway.exception.PaymentProcessingException;
import com.brillio.paymentgateway.model.Payment;
import com.brillio.paymentgateway.model.PaymentMethod;
import com.brillio.paymentgateway.model.PaymentStatus;
import com.brillio.paymentgateway.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    void createPayment_authorizesSuccessfully() {
        PaymentRequest req = PaymentRequest.builder()
                .orderId("ord-" + System.nanoTime())
                .customerId("cust-1")
                .amount(new BigDecimal("100.00"))
                .currency("INR")
                .method(PaymentMethod.CREDIT_CARD)
                .build();

        Payment created = paymentService.createPayment(req);
        assertEquals(PaymentStatus.AUTHORIZED, created.getStatus());
        assertNotNull(created.getGatewayTransactionId());
    }

    @Test
    void createPayment_failsOn_13_suffix() {
        PaymentRequest req = PaymentRequest.builder()
                .orderId("ord-" + System.nanoTime())
                .customerId("cust-1")
                .amount(new BigDecimal("100.13"))
                .currency("INR")
                .method(PaymentMethod.CREDIT_CARD)
                .build();

        Payment created = paymentService.createPayment(req);
        assertEquals(PaymentStatus.FAILED, created.getStatus());
        assertNotNull(created.getFailureReason());
    }

    @Test
    void duplicateOrderId_throws() {
        String orderId = "ord-" + System.nanoTime();
        PaymentRequest req = PaymentRequest.builder()
                .orderId(orderId)
                .customerId("cust-1")
                .amount(new BigDecimal("50.00"))
                .currency("INR")
                .method(PaymentMethod.UPI)
                .build();
        paymentService.createPayment(req);
        assertThrows(PaymentProcessingException.class, () -> paymentService.createPayment(req));
    }
}
