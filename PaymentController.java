package com.paymentgateway.controller;

import com.paymentgateway.dto.PaymentRequest;
import com.paymentgateway.dto.PaymentResponse;
import com.paymentgateway.dto.RefundRequest;
import com.paymentgateway.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(@Valid @RequestBody PaymentRequest request) {
        return PaymentResponse.from(paymentService.createPayment(request));
    }

    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable UUID id) {
        return PaymentResponse.from(paymentService.getPayment(id));
    }

    @GetMapping
    public List<PaymentResponse> listPayments() {
        return paymentService.listPayments().stream().map(PaymentResponse::from).toList();
    }

    @PostMapping("/{id}/capture")
    public PaymentResponse capture(@PathVariable UUID id) {
        return PaymentResponse.from(paymentService.capturePayment(id));
    }

    @PostMapping("/{id}/refund")
    public PaymentResponse refund(@PathVariable UUID id, @Valid @RequestBody RefundRequest request) {
        return PaymentResponse.from(paymentService.refundPayment(id, request));
    }

    @PostMapping("/{id}/cancel")
    public PaymentResponse cancel(@PathVariable UUID id) {
        return PaymentResponse.from(paymentService.cancelPayment(id));
    }
}
