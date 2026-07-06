package com.letstravel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letstravel.config.AppProperties;
import com.letstravel.domain.Payment;
import com.letstravel.exception.BusinessException;
import com.letstravel.repository.PaymentRepository;
import com.letstravel.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayPalService {

    private final AppProperties appProperties;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String getBaseUrl() {
        return "sandbox".equals(appProperties.getPaypal().getMode())
            ? "https://api-m.sandbox.paypal.com"
            : "https://api-m.paypal.com";
    }

    @SuppressWarnings("unchecked")
    private String getAccessToken() {
        String credentials = appProperties.getPaypal().getClientId() + ":" + appProperties.getPaypal().getClientSecret();
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/v1/oauth2/token"))
                .header("Authorization", "Basic " + encoded)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> body = objectMapper.readValue(resp.body(), Map.class);
            return (String) body.get("access_token");
        } catch (Exception e) {
            throw new BusinessException("Failed to get PayPal access token: " + e.getMessage(), HttpStatus.BAD_GATEWAY);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> createOrder(BigDecimal amount, String currency, Long subscriptionId, Long paymentId) {
        try {
            String token = getAccessToken();
            Map<String, Object> orderBody = Map.of(
                "intent", "CAPTURE",
                "purchase_units", new Object[]{Map.of(
                    "amount", Map.of("currency_code", currency, "value", amount.toPlainString()),
                    "custom_id", subscriptionId.toString()
                )},
                "application_context", Map.of(
                    "return_url", "http://localhost:4200/payment/success",
                    "cancel_url", "http://localhost:4200/payment/cancel"
                )
            );
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/v2/checkout/orders"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(orderBody)))
                .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> body = objectMapper.readValue(resp.body(), Map.class);
            String orderId = (String) body.get("id");

            Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("Payment not found", HttpStatus.NOT_FOUND));
            payment.setProviderRef(orderId);
            paymentRepository.save(payment);

            // Extract approval URL
            java.util.List<Map<String, Object>> links = (java.util.List<Map<String, Object>>) body.get("links");
            String approvalUrl = links.stream()
                .filter(l -> "approve".equals(l.get("rel")))
                .map(l -> (String) l.get("href"))
                .findFirst().orElse(null);

            Map<String, String> result = new HashMap<>();
            result.put("orderId", orderId);
            result.put("approvalUrl", approvalUrl);
            return result;
        } catch (Exception e) {
            throw new BusinessException("PayPal order creation failed: " + e.getMessage(), HttpStatus.BAD_GATEWAY);
        }
    }

    @SuppressWarnings("unchecked")
    public void captureOrder(String orderId) {
        try {
            String token = getAccessToken();
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/v2/checkout/orders/" + orderId + "/capture"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
            httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            paymentRepository.findByProviderRef(orderId).ifPresent(payment ->
                subscriptionRepository.findByPaymentId(payment.getId()).ifPresent(sub -> {
                    try {
                        subscriptionService.confirmSubscription(sub.getId());
                    } catch (Exception e) {
                        log.error("Failed to confirm subscription {} after PayPal capture: {}", sub.getId(), e.getMessage());
                    }
                })
            );
            log.info("PayPal order captured: {}", orderId);
        } catch (Exception e) {
            throw new BusinessException("PayPal capture failed: " + e.getMessage(), HttpStatus.BAD_GATEWAY);
        }
    }

    @SuppressWarnings("unchecked")
    public void handleWebhookEvent(Map<String, Object> event) {
        String eventType = (String) event.get("event_type");
        if ("PAYMENT.CAPTURE.COMPLETED".equals(eventType)) {
            Map<String, Object> resource = (Map<String, Object>) event.get("resource");
            if (resource != null) {
                String orderId = extractOrderId(resource);
                if (orderId != null) {
                    confirmByOrderId(orderId);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String extractOrderId(Map<String, Object> resource) {
        try {
            Map<String, Object> suppData = (Map<String, Object>) resource.get("supplementary_data");
            if (suppData != null) {
                Map<String, Object> relatedIds = (Map<String, Object>) suppData.get("related_ids");
                if (relatedIds != null) {
                    return (String) relatedIds.get("order_id");
                }
            }
        } catch (Exception e) {
            log.warn("Could not extract order ID from PayPal webhook resource: {}", e.getMessage());
        }
        return null;
    }

    private void confirmByOrderId(String orderId) {
        paymentRepository.findByProviderRef(orderId).ifPresent(payment ->
            subscriptionRepository.findByPaymentId(payment.getId()).ifPresent(sub -> {
                try {
                    subscriptionService.confirmSubscription(sub.getId());
                } catch (Exception e) {
                    log.error("Failed to confirm subscription {} from PayPal webhook: {}", sub.getId(), e.getMessage());
                }
            })
        );
    }
}
