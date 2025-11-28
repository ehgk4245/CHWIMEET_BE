package com.back.domain.report.repository;

import com.back.domain.report.common.ReportType;
import com.back.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByMemberIdAndTargetIdAndReportType(
            Long memberId,
            Long targetId,
            ReportType reportType
    );
}