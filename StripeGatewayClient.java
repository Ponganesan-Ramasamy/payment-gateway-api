package com.brillio.paymentgateway.service.stripe;

import com.brillio.paymentgateway.dto.PaymentRequest;
import com.brillio.paymentgateway.dto.RefundRequest;
import com.brillio.paymentgateway.service.PaymentGatewayClient;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCaptureParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@ConditionalOnProperty(name = "payment.gateway.provider", havingValue = "stripe")
@EnableConfigurationProperties(StripeProperties.class)
@RequiredArgsConstructor
@Slf4j
public class StripeGatewayClient implements PaymentGatewayClient {

    private final StripeProperties props;

    @PostConstruct
    void init() {
        Stripe.apiKey = props.getApiKey();
        log.info("Stripe gateway client initialized");
    }

    @Override
    public GatewayResult authorize(PaymentRequest request) {
        try {
            long amountMinorUnits = toMinorUnits(request.getAmount(), request.getCurrency());
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountMinorUnits)
                    .setCurrency(request.getCurrency().toLowerCase())
                    .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
                    .setConfirm(true)
                    .setPaymentMethod(request.getCardToken() != null ? request.getCardToken() : "pm_card_visa")
                    .putMetadata("orderId", request.getOrderId())
                    .putMetadata("customerId", request.getCustomerId())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build())
                    .build();
            PaymentIntent intent = PaymentIntent.create(params);
            if ("requires_capture".equals(intent.getStatus()) || "succeeded".equals(intent.getStatus())) {
                return GatewayResult.ok(intent.getId());
            }
            return GatewayResult.fail("Stripe status: " + intent.getStatus());
        } catch (StripeException e) {
            log.warn("Stripe authorize failed", e);
            return GatewayResult.fail(e.getMessage());
        }
    }

    @Override
    public GatewayResult capture(String paymentIntentId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            if ("succeeded".equals(intent.getStatus())) {
                return GatewayResult.ok(intent.getId());
            }
            PaymentIntent captured = intent.capture(PaymentIntentCaptureParams.builder().build());
            return GatewayResult.ok(captured.getId());
        } catch (StripeException e) {
            log.warn("Stripe capture failed", e);
            return GatewayResult.fail(e.getMessage());
        }
    }

    @Override
    public GatewayResult refund(String paymentIntentId, RefundRequest request) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            long amountMinorUnits = toMinorUnits(request.getAmount(), intent.getCurrency());
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .setAmount(amountMinorUnits)
                    .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                    .build();
            Refund refund = Refund.create(params);
            return GatewayResult.ok(refund.getId());
        } catch (StripeException e) {
            log.warn("Stripe refund failed", e);
            return GatewayResult.fail(e.getMessage());
        }
    }

    private long toMinorUnits(BigDecimal amount, String currency) {
        // Zero-decimal currencies per Stripe docs
        String c = currency.toLowerCase();
        boolean zeroDecimal = c.equals("jpy") || c.equals("krw") || c.equals("vnd");
        BigDecimal factor = BigDecimal.valueOf(zeroDecimal ? 1 : 100);
        return amount.multiply(factor).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
