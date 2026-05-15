package com.paymentgateway.dto;

import com.paymentgateway.model.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotBlank(message = "orderId is required")
    private String orderId;

    @NotBlank(message = "customerId is required")
    private String customerId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3, message = "currency must be a 3-letter ISO code")
    private String currency;

    @NotNull(message = "method is required")
    private PaymentMethod method;

    private String cardToken;
    private String upiId;
}
