package com.letstravel.controller;

import com.letstravel.service.PayPalService;
import com.letstravel.service.StripeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks")
public class WebhookController {

    private final StripeService stripeService;
    private final PayPalService payPalService;

    @PostMapping("/stripe")
    public ResponseEntity<Void> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        stripeService.handleWebhookEvent(payload, sigHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/paypal")
    public ResponseEntity<Void> paypalWebhook(@RequestBody Map<String, Object> event) {
        payPalService.handleWebhookEvent(event);
        return ResponseEntity.ok().build();
    }
}
