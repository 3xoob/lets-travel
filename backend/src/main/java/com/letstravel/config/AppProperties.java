package com.letstravel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private Stripe stripe = new Stripe();
    private Paypal paypal = new Paypal();
    private Uploads uploads = new Uploads();

    @Data
    public static class Jwt {
        private String secret;
        private long accessTokenExpiryMs = 900_000L;
        private long refreshTokenExpiryMs = 604_800_000L;
    }

    @Data
    public static class Cors {
        private String allowedOrigins = "http://localhost:4200";
    }

    @Data
    public static class Stripe {
        private String secretKey;
        private String webhookSecret;
        private String publishableKey;
    }

    @Data
    public static class Paypal {
        private String clientId;
        private String clientSecret;
        private String mode = "sandbox";
    }

    @Data
    public static class Uploads {
        private String dir = "/uploads";
    }
}
