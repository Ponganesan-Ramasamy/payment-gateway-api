package com.paymentgateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentGatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payment Gateway Service API")
                        .description("REST API for processing, capturing, and refunding payments.")
                        .version("1.0.0"));
    }
}
