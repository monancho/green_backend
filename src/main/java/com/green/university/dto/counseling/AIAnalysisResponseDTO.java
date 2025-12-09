package com.green.university.dto.counseling;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

/**
 * AI 분석 응답 DTO (LLM으로부터 받는 데이터)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisResponseDTO {
    private Integer riskScore;
    private String riskLevel;
    private List<String> mainFactors;
    private List<String> recommendedActions;
    private String summary;
}

