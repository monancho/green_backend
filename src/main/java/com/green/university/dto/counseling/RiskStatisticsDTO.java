package com.green.university.dto.counseling;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
/**
 * 위험도 통계 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskStatisticsDTO {
    private Integer totalStudents;
    private Integer lowRiskCount;
    private Integer mediumRiskCount;
    private Integer highRiskCount;
    private Integer criticalRiskCount;
    private Double averageRiskScore;
    private List<RiskStudentListDTO> topRiskStudents;
}