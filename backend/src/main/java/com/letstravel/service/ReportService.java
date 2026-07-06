package com.letstravel.service;

import com.letstravel.domain.Report;
import com.letstravel.domain.User;
import com.letstravel.domain.enums.ReportStatus;
import com.letstravel.dto.report.ReportRequest;
import com.letstravel.dto.report.ReportResponse;
import com.letstravel.dto.report.ResolveReportRequest;
import com.letstravel.exception.EntityNotFoundException;
import com.letstravel.repository.ManagerProfileRepository;
import com.letstravel.repository.ReportRepository;
import com.letstravel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ManagerProfileRepository managerProfileRepository;

    @Transactional
    public ReportResponse submitReport(ReportRequest req, String reporterEmail) {
        User reporter = userRepository.findByEmail(reporterEmail)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        Report report = Report.builder()
            .reporter(reporter)
            .targetType(req.targetType())
            .targetId(req.targetId())
            .reason(req.reason())
            .detail(req.detail())
            .status(ReportStatus.OPEN)
            .build();
        return toResponse(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public Page<ReportResponse> getReports(ReportStatus status, Pageable pageable) {
        if (status != null) {
            return reportRepository.findByStatus(status, pageable).map(this::toResponse);
        }
        return reportRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReportResponse> getMyReports(String email, Pageable pageable) {
        User reporter = userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        return reportRepository.findByReporterId(reporter.getId(), pageable)
            .map(this::toResponse);
    }

    @Transactional
    public ReportResponse resolveReport(Long reportId, ResolveReportRequest req, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new EntityNotFoundException("Report", reportId));
        report.setStatus(req.status());
        report.setReviewedBy(admin);
        report.setReviewedAt(LocalDateTime.now());
        report = reportRepository.save(report);

        // If resolved against a manager, increment their report count
        if (req.status() == ReportStatus.RESOLVED
                && report.getTargetType() == com.letstravel.domain.enums.ReportTargetType.MANAGER) {
            managerProfileRepository.findByUserId(report.getTargetId()).ifPresent(mp -> {
                mp.setReportCount(mp.getReportCount() + 1);
                managerProfileRepository.save(mp);
            });
        }
        return toResponse(report);
    }

    private ReportResponse toResponse(Report r) {
        return new ReportResponse(
            r.getId(), r.getReporter().getId(),
            r.getReporter().getFirstName() + " " + r.getReporter().getLastName(),
            r.getTargetType(), r.getTargetId(),
            r.getReason(), r.getDetail(),
            r.getStatus(), r.getCreatedAt());
    }
}
