package com.green.university.dto.counseling;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
/**
 * 위험 학생 목록 조회용 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskStudentListDTO {
    private Integer studentId;
    private String studentName;
    private String departmentName;
    private Integer grade;
    private Integer totalRiskScore;
    private String riskLevel;
    private Integer counselingRiskScore;
    private Integer gradeRiskScore;
    private Integer attendanceRiskScore;
    private Date lastCounselingDate;
    private Timestamp lastUpdated;
}