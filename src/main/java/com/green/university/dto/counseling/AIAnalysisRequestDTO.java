package com.green.university.dto.counseling;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

/**
 * AI 분석 요청 DTO (LLM에 전달할 데이터)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisRequestDTO {
    private String studentName;
    private String counselingType;
    private String content;
    private Date counselingDate;

    // 추가 컨텍스트 정보
    private Double currentGPA;
    private Integer totalAbsences;
    private Integer currentSemester;
}