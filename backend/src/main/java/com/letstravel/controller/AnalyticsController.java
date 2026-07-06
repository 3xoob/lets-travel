package com.letstravel.controller;

import com.letstravel.dto.analytics.*;
import com.letstravel.service.AnalyticsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/admin/analytics/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponse> adminSummary(
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(analyticsService.getAdminDashboard(months));
    }

    @GetMapping("/admin/analytics/income")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<IncomeByMonthDto>> adminIncome(
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(analyticsService.getAdminIncomeByMonth(months));
    }

    @GetMapping("/admin/analytics/top-managers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ManagerRankDto>> topManagers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopManagers(limit));
    }

    @GetMapping("/manager/analytics/summary")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ManagerDashboardResponse> managerSummary(
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(analyticsService.getManagerDashboard(ud.getUsername()));
    }

    @GetMapping("/manager/analytics/income")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<List<IncomeByMonthDto>> managerIncome(
            @RequestParam(defaultValue = "6") int months,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(analyticsService.getManagerIncomeByMonth(ud.getUsername(), months));
    }
}
