package com.letstravel.controller;

import com.letstravel.dto.payment.PaymentInitResponse;
import com.letstravel.dto.subscription.ManagerUnsubscribeRequest;
import com.letstravel.dto.subscription.SubscribeRequest;
import com.letstravel.dto.subscription.SubscriptionResponse;
import com.letstravel.service.SubscriptionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscriptions")
    public ResponseEntity<PaymentInitResponse> subscribe(
            @Valid @RequestBody SubscribeRequest req,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.status(201).body(subscriptionService.subscribe(req, ud.getUsername()));
    }

    @DeleteMapping("/subscriptions/{id}")
    public ResponseEntity<Void> unsubscribe(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails ud) {
        subscriptionService.unsubscribe(id, ud.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subscriptions/my")
    public ResponseEntity<Page<SubscriptionResponse>> getMySubscriptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(subscriptionService.getMySubscriptions(ud.getUsername(), PageRequest.of(page, size)));
    }

    @GetMapping("/manager/travels/{travelId}/subscribers")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<List<SubscriptionResponse>> getSubscribers(
            @PathVariable Long travelId,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(subscriptionService.getTravelSubscribers(travelId, ud.getUsername()));
    }

    @DeleteMapping("/manager/subscriptions/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<Void> managerUnsubscribe(
            @PathVariable Long id,
            @Valid @RequestBody ManagerUnsubscribeRequest req,
            @AuthenticationPrincipal UserDetails ud) {
        subscriptionService.managerUnsubscribe(id, req, ud.getUsername());
        return ResponseEntity.noContent().build();
    }
}
