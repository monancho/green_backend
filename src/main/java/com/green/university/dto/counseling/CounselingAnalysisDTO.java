package com.green.university.dto.counseling;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;


/**
 * AI 분석 결과 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CounselingAnalysisDTO {
    private Integer id;
    private Integer counselingId;
    private Integer riskScore;
    private String riskLevel;
    private List<String> mainFactors;
    private List<String> recommendedActions;
    private Timestamp analysisDate;
}