package com.paymentgateway.service;

import com.paymentgateway.dto.PaymentRequest;
import com.paymentgateway.dto.RefundRequest;

public interface PaymentGatewayClient {

    GatewayResult authorize(PaymentRequest request);

    GatewayResult capture(String gatewayTransactionId);

    GatewayResult refund(String gatewayTransactionId, RefundRequest request);

    record GatewayResult(boolean success, String gatewayTransactionId, String failureReason) {
        public static GatewayResult ok(String txnId) {
            return new GatewayResult(true, txnId, null);
        }

        public static GatewayResult fail(String reason) {
            return new GatewayResult(false, null, reason);
        }
    }
}
