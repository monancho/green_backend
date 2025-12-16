package com.green.university.repository.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 상담 분석 결과를 나타내는 JPA 엔티티
 * 각 상담 기록에 대한 중도이탈 위험도 분석 결과를 저장합니다.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "counseling_analysis_tb")
public class CounselingAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * 분석 대상 상담 기록 ID
     */
    @Column(name = "counseling_id", nullable = false)
    private Integer counselingId;

    /**
     * 분석 대상 상담 기록 (읽기 전용)
     */
    @OneToOne
    @JoinColumn(name = "counseling_id", insertable = false, updatable = false)
    private Counseling counseling;

    /**
     * 위험도 점수 (0~100)
     */
    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    /**
     * 위험도 레벨: LOW, MEDIUM, HIGH
     */
    @Column(name = "risk_level", nullable = false)
    private String riskLevel;

    /**
     * 주요 위험 요인 (JSON 형태 저장)
     * 예: ["경제적 부담", "학업 스트레스", "과제 미이행"]
     */
    @Column(name = "main_factors", columnDefinition = "TEXT")
    private String mainFactors;

    /**
     * AI 권장 조치사항 (JSON 형태 저장)
     * 예: ["장학금 안내", "학업 코칭", "정기 모니터링"]
     */
    @Column(name = "recommended_actions", columnDefinition = "TEXT")
    private String recommendedActions;

    /**
     * 분석 일시
     */
    @Column(name = "analysis_date")
    private Timestamp analysisDate;
}