package com.letstravel.controller;

import com.letstravel.dto.feedback.FeedbackRequest;
import com.letstravel.dto.feedback.FeedbackResponse;
import com.letstravel.service.FeedbackService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/feedback")
    public ResponseEntity<FeedbackResponse> submit(
            @Valid @RequestBody FeedbackRequest req,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.status(201).body(feedbackService.submitFeedback(req, ud.getUsername()));
    }

    @GetMapping("/travels/{travelId}/feedback")
    public ResponseEntity<Page<FeedbackResponse>> getForTravel(
            @PathVariable Long travelId,
            Pageable pageable) {
        return ResponseEntity.ok(feedbackService.getForTravel(travelId, pageable));
    }

    @GetMapping("/manager/feedback")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<Page<FeedbackResponse>> getForManager(
            Pageable pageable,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(feedbackService.getForManager(ud.getUsername(), pageable));
    }
}
