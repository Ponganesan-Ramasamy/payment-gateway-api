package com.brillio.paymentgateway.service;

import com.brillio.paymentgateway.dto.PaymentRequest;
import com.brillio.paymentgateway.dto.RefundRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Stub gateway client. Active only when payment.gateway.provider=mock.
 */
@Component
@ConditionalOnProperty(name = "payment.gateway.provider", havingValue = "mock", matchIfMissing = true)
public class MockPaymentGatewayClient implements PaymentGatewayClient {

    @Override
    public GatewayResult authorize(PaymentRequest request) {
        if (request.getAmount().toPlainString().endsWith(".13")) {
            return GatewayResult.fail("Card declined by issuer");
        }
        return GatewayResult.ok("auth_" + UUID.randomUUID());
    }

    @Override
    public GatewayResult capture(String gatewayTransactionId) {
        return GatewayResult.ok(gatewayTransactionId.replace("auth_", "cap_"));
    }

    @Override
    public GatewayResult refund(String gatewayTransactionId, RefundRequest request) {
        return GatewayResult.ok("rfnd_" + UUID.randomUUID());
    }
}
