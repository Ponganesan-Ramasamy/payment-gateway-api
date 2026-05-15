package com.paymentgateway.service.razorpay;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "payment.gateway.razorpay")
public class RazorpayProperties {
    private String keyId;
    private String keySecret;
}
