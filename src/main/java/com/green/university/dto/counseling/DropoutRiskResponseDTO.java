package com.green.university.dto.counseling;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
/**
 * 통합 위험도 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DropoutRiskResponseDTO {
    private Integer id;
    private Integer studentId;
    private String studentName;
    private String departmentName;
    private Integer year;
    private Integer semester;
    private Integer gradeRiskScore;
    private Integer attendanceRiskScore;
    private Integer counselingRiskScore;
    private Integer totalRiskScore;
    private String riskLevel;
    private Timestamp lastUpdated;

    // 최근 상담 정보
    private Date lastCounselingDate;
    private String lastCounselingType;
}