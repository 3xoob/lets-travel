package com.letstravel.service;

import com.letstravel.config.AppProperties;
import com.letstravel.domain.Payment;
import com.letstravel.exception.BusinessException;
import com.letstravel.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    private final AppProperties appProperties;
    private final PaymentRepository paymentRepository;
    private final SubscriptionService subscriptionService;

    @PostConstruct
    public void init() {
        Stripe.apiKey = appProperties.getStripe().getSecretKey();
    }

    public Map<String, String> createPaymentIntent(BigDecimal amount, String currency, Long subscriptionId, Long paymentId) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                .setCurrency(currency.toLowerCase())
                .putMetadata("subscriptionId", subscriptionId.toString())
                .putMetadata("paymentId", paymentId.toString())
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true).build())
                .build();
            PaymentIntent intent = PaymentIntent.create(params);
            Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("Payment not found", HttpStatus.NOT_FOUND));
            payment.setProviderIntentId(intent.getId());
            paymentRepository.save(payment);
            Map<String, String> result = new HashMap<>();
            result.put("clientSecret", intent.getClientSecret());
            result.put("paymentIntentId", intent.getId());
            return result;
        } catch (StripeException e) {
            log.error("Stripe error creating payment intent", e);
            throw new BusinessException("Payment initialization failed: " + e.getMessage(), HttpStatus.BAD_GATEWAY);
        }
    }

    public void handleWebhookEvent(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, appProperties.getStripe().getWebhookSecret());
        } catch (SignatureVerificationException e) {
            throw new BusinessException("Invalid Stripe webhook signature", HttpStatus.BAD_REQUEST);
        }
        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
                if (deserializer.getObject().isPresent()) {
                    PaymentIntent intent = (PaymentIntent) deserializer.getObject().get();
                    String subIdStr = intent.getMetadata().get("subscriptionId");
                    if (subIdStr != null) {
                        subscriptionService.confirmSubscription(Long.parseLong(subIdStr));
                    }
                }
            }
            case "payment_intent.payment_failed" -> {
                EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
                if (deserializer.getObject().isPresent()) {
                    PaymentIntent intent = (PaymentIntent) deserializer.getObject().get();
                    String subIdStr = intent.getMetadata().get("subscriptionId");
                    if (subIdStr != null) {
                        subscriptionService.failSubscription(Long.parseLong(subIdStr));
                    }
                }
            }
            default -> log.debug("Unhandled Stripe event: {}", event.getType());
        }
    }

    public void createRefund(String paymentIntentId, BigDecimal amount) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                .build();
            Refund.create(params);
        } catch (StripeException e) {
            log.error("Stripe refund error", e);
            throw new BusinessException("Refund failed: " + e.getMessage(), HttpStatus.BAD_GATEWAY);
        }
    }
}
