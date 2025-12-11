package com.green.university.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StuSubResponseDto {

    private Integer studentId;
    private String studentName;
    private String deptName;

    private Integer absent;
    private Integer lateness;
    private Integer homework;
    private Integer midExam;
    private Integer finalExam;
    private Integer convertedMark;
}