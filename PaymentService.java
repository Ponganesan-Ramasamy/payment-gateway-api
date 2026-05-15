package com.brillio.paymentgateway.service;

import com.brillio.paymentgateway.dto.PaymentRequest;
import com.brillio.paymentgateway.dto.RefundRequest;
import com.brillio.paymentgateway.exception.PaymentNotFoundException;
import com.brillio.paymentgateway.exception.PaymentProcessingException;
import com.brillio.paymentgateway.model.Payment;
import com.brillio.paymentgateway.model.PaymentRepository;
import com.brillio.paymentgateway.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository repository;
    private final PaymentGatewayClient gatewayClient;

    @Transactional
    public Payment createPayment(PaymentRequest request) {
        repository.findByOrderId(request.getOrderId()).ifPresent(p -> {
            throw new PaymentProcessingException("Payment for orderId " + request.getOrderId() + " already exists");
        });

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .currency(request.getCurrency().toUpperCase())
                .method(request.getMethod())
                .status(PaymentStatus.PENDING)
                .build();

        PaymentGatewayClient.GatewayResult result = gatewayClient.authorize(request);
        if (result.success()) {
            payment.setStatus(PaymentStatus.AUTHORIZED);
            payment.setGatewayTransactionId(result.gatewayTransactionId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(result.failureReason());
        }

        Payment saved = repository.save(payment);
        log.info("Payment {} created with status {}", saved.getId(), saved.getStatus());
        return saved;
    }

    @Transactional
    public Payment capturePayment(UUID id) {
        Payment payment = getPayment(id);
        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
            throw new PaymentProcessingException("Only AUTHORIZED payments can be captured");
        }
        PaymentGatewayClient.GatewayResult result = gatewayClient.capture(payment.getGatewayTransactionId());
        if (!result.success()) {
            throw new PaymentProcessingException(result.failureReason());
        }
        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setGatewayTransactionId(result.gatewayTransactionId());
        return repository.save(payment);
    }

    @Transactional
    public Payment refundPayment(UUID id, RefundRequest request) {
        Payment payment = getPayment(id);
        if (payment.getStatus() != PaymentStatus.CAPTURED) {
            throw new PaymentProcessingException("Only CAPTURED payments can be refunded");
        }
        if (request.getAmount().compareTo(payment.getAmount()) > 0) {
            throw new PaymentProcessingException("Refund amount exceeds captured amount");
        }
        PaymentGatewayClient.GatewayResult result = gatewayClient.refund(payment.getGatewayTransactionId(), request);
        if (!result.success()) {
            throw new PaymentProcessingException(result.failureReason());
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        return repository.save(payment);
    }

    @Transactional
    public Payment cancelPayment(UUID id) {
        Payment payment = getPayment(id);
        if (payment.getStatus() != PaymentStatus.AUTHORIZED && payment.getStatus() != PaymentStatus.PENDING) {
            throw new PaymentProcessingException("Only PENDING or AUTHORIZED payments can be cancelled");
        }
        payment.setStatus(PaymentStatus.CANCELLED);
        return repository.save(payment);
    }

    public Payment getPayment(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment " + id + " not found"));
    }

    public List<Payment> listPayments() {
        return repository.findAll();
    }
}
