package com.brillio.paymentgateway.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "security.users")
public class UsersProperties {
    private String adminUsername;
    private String adminPassword;
}
