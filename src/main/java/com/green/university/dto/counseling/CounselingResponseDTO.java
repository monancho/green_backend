package com.green.university.dto.counseling;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
/**
 * 상담 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CounselingResponseDTO {
    private Integer id;
    private Integer studentId;
    private String studentName;
    private Integer counselorId;
    private String counselorName;
    private String counselorType;
    private String title;
    private String content;
    private String counselingType;
    private Date counselingDate;
    private Timestamp createdAt;

    // AI 분석 결과 포함
    private CounselingAnalysisDTO analysis;
}