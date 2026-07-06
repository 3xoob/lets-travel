package com.letstravel.controller;

import com.letstravel.dto.travel.TravelSummary;
import com.letstravel.service.RecommendationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<List<TravelSummary>> getRecommendations(
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(recommendationService.getRecommendations(ud.getUsername()));
    }

    @GetMapping("/trending")
    public ResponseEntity<List<TravelSummary>> getTrending() {
        return ResponseEntity.ok(recommendationService.getTrending());
    }
}
