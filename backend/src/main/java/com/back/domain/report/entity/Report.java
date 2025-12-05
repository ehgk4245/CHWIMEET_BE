package com.back.domain.report.entity;

import com.back.domain.member.entity.Member;
import com.back.domain.report.common.ReportType;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Report extends BaseEntity {

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "comment",nullable = false)
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type",nullable = false)
    private ReportType reportType;

    public static Report create(Long targetId, String comment, Member member, ReportType reportType) {
        return new Report(targetId, comment, member, reportType);
    }

    public static Report createPostType(Long targetId, String comment, Member member) {
        return new Report(targetId, comment, member, ReportType.POST);
    }

    public static Report createMemberType(Long targetId, String comment, Member member) {
        return new Report(targetId, comment, member, ReportType.MEMBER);
    }

    public static Report createReviewType(Long targetId, String comment, Member member) {
        return new Report(targetId, comment, member, ReportType.REVIEW);
    }
}
