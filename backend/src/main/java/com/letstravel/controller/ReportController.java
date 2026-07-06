package com.letstravel.controller;

import com.letstravel.domain.enums.ReportStatus;
import com.letstravel.dto.report.ReportRequest;
import com.letstravel.dto.report.ReportResponse;
import com.letstravel.dto.report.ResolveReportRequest;
import com.letstravel.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Reports")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/reports/my")
    public ResponseEntity<Page<ReportResponse>> getMyReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(reportService.getMyReports(ud.getUsername(), PageRequest.of(page, size)));
    }

    @PostMapping("/reports")
    public ResponseEntity<ReportResponse> submit(
            @Valid @RequestBody ReportRequest req,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.status(201).body(reportService.submitReport(req, ud.getUsername()));
    }

    @GetMapping("/admin/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReportResponse>> getReports(
            @RequestParam(required = false) ReportStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(reportService.getReports(status, pageable));
    }

    @PatchMapping("/admin/reports/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportResponse> resolve(
            @PathVariable Long id,
            @Valid @RequestBody ResolveReportRequest req,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(reportService.resolveReport(id, req, ud.getUsername()));
    }
}
