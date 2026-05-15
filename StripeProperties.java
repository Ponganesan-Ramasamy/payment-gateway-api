package com.paymentgateway.service.stripe;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "payment.gateway.stripe")
public class StripeProperties {
    private String apiKey;
    private String captureMethod = "automatic";
}
