package com.letstravel.repository;

import com.letstravel.domain.Report;
import com.letstravel.domain.enums.ReportStatus;
import com.letstravel.domain.enums.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    Page<Report> findByReporterId(Long reporterId, Pageable pageable);

    long countByStatus(ReportStatus status);

    long countByTargetTypeAndTargetId(ReportTargetType targetType, Long targetId);
}
