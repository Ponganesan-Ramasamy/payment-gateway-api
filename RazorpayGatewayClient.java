package com.brillio.paymentgateway.service.razorpay;

import com.brillio.paymentgateway.dto.PaymentRequest;
import com.brillio.paymentgateway.dto.RefundRequest;
import com.brillio.paymentgateway.service.PaymentGatewayClient;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Razorpay integration. Razorpay's typical server-side flow is:
 *  1. Create an Order (authorize-equivalent).
 *  2. Client completes payment via Razorpay Checkout, which yields a paymentId.
 *  3. Server captures the payment by paymentId (capture-equivalent).
 *  4. Server can refund by paymentId.
 *
 * Our `authorize` creates the Order and returns its id. `capture` expects
 * the Razorpay payment id (passed in via gatewayTransactionId after client-side
 * checkout). For end-to-end automation, integrate webhook handling.
 */
@Component
@ConditionalOnProperty(name = "payment.gateway.provider", havingValue = "razorpay")
@EnableConfigurationProperties(RazorpayProperties.class)
@Slf4j
public class RazorpayGatewayClient implements PaymentGatewayClient {

    private final RazorpayClient client;

    public RazorpayGatewayClient(RazorpayProperties props) throws RazorpayException {
        this.client = new RazorpayClient(props.getKeyId(), props.getKeySecret());
        log.info("Razorpay gateway client initialized");
    }

    @Override
    public GatewayResult authorize(PaymentRequest request) {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", toMinorUnits(request.getAmount()));
            orderRequest.put("currency", request.getCurrency().toUpperCase());
            orderRequest.put("receipt", request.getOrderId());
            orderRequest.put("payment_capture", 0);
            JSONObject notes = new JSONObject();
            notes.put("customerId", request.getCustomerId());
            orderRequest.put("notes", notes);

            Order order = client.orders.create(orderRequest);
            return GatewayResult.ok(order.get("id"));
        } catch (RazorpayException e) {
            log.warn("Razorpay order create failed", e);
            return GatewayResult.fail(e.getMessage());
        }
    }

    @Override
    public GatewayResult capture(String paymentId) {
        try {
            Payment payment = client.payments.fetch(paymentId);
            BigDecimal amountInMinorUnits = new BigDecimal(payment.get("amount").toString());
            JSONObject captureRequest = new JSONObject();
            captureRequest.put("amount", amountInMinorUnits.longValueExact());
            captureRequest.put("currency", payment.get("currency").toString());
            Payment captured = client.payments.capture(paymentId, captureRequest);
            return GatewayResult.ok(captured.get("id"));
        } catch (RazorpayException e) {
            log.warn("Razorpay capture failed", e);
            return GatewayResult.fail(e.getMessage());
        }
    }

    @Override
    public GatewayResult refund(String paymentId, RefundRequest request) {
        try {
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", toMinorUnits(request.getAmount()));
            if (request.getReason() != null) {
                JSONObject notes = new JSONObject();
                notes.put("reason", request.getReason());
                refundRequest.put("notes", notes);
            }
            Refund refund = client.payments.refund(paymentId, refundRequest);
            return GatewayResult.ok(refund.get("id"));
        } catch (RazorpayException e) {
            log.warn("Razorpay refund failed", e);
            return GatewayResult.fail(e.getMessage());
        }
    }

    private long toMinorUnits(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
