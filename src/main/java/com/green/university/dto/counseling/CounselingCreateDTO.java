package com.green.university.dto.counseling;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 상담 등록 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CounselingCreateDTO {
    private Integer studentId;
    private Integer counselorId;
    private String counselorType;  // PROFESSOR, STAFF
    private String title;
    private String content;
    private String counselingType;  // 학업, 정서, 가정, 경력, 기타
    private Date counselingDate;
}