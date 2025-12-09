package com.green.university.repository.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 학생의 통합 중도이탈 위험도를 나타내는 엔티티
 * 성적, 출석, 상담 기반 위험도를 종합하여 관리합니다.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dropout_risk_tb")
public class DropoutRisk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * 위험도 대상 학생 ID
     */
    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    /**
     * 위험도 대상 학생 (읽기 전용)
     */
    @ManyToOne
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private Student student;

    /**
     * 대상 년도
     */
    @Column(name = "year", nullable = false)
    private Integer year;

    /**
     * 대상 학기
     */
    @Column(name = "semester", nullable = false)
    private Integer semester;

    /**
     * 성적 기반 위험도 (0~100)
     */
    @Column(name = "grade_risk_score")
    private Integer gradeRiskScore = 0;

    /**
     * 출석 기반 위험도 (0~100)
     */
    @Column(name = "attendance_risk_score")
    private Integer attendanceRiskScore = 0;

    /**
     * 상담 기반 위험도 (0~100)
     */
    @Column(name = "counseling_risk_score")
    private Integer counselingRiskScore = 0;

    /**
     * 총합 위험도 (가중 평균)
     */
    @Column(name = "total_risk_score")
    private Integer totalRiskScore = 0;

    /**
     * 위험 레벨: LOW, MEDIUM, HIGH, CRITICAL
     */
    @Column(name = "risk_level")
    private String riskLevel;

    /**
     * 마지막 업데이트 시간
     */
    @Column(name = "last_updated")
    private Timestamp lastUpdated;

    /**
     * 총합 위험도 계산 (가중 평균)
     * 상담 40%, 성적 35%, 출석 25%
     */
    public void calculateTotalRiskScore() {
        // null-safe 처리
        int counselingScore = counselingRiskScore != null ? counselingRiskScore : 0;
        int gradeScore = gradeRiskScore != null ? gradeRiskScore : 0;
        int attendanceScore = attendanceRiskScore != null ? attendanceRiskScore : 0;

        this.totalRiskScore = (int) Math.round(
                counselingScore * 0.4 +
                        gradeScore * 0.35 +
                        attendanceScore * 0.25
        );

        // 위험 레벨 자동 설정
        if (totalRiskScore >= 80) {
            this.riskLevel = "CRITICAL";
        } else if (totalRiskScore >= 60) {
            this.riskLevel = "HIGH";
        } else if (totalRiskScore >= 40) {
            this.riskLevel = "MEDIUM";
        } else {
            this.riskLevel = "LOW";
        }
    }
}