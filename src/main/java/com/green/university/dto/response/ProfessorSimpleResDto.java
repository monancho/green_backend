package com.green.university.dto.response;

import lombok.Data;

@Data
public class ProfessorSimpleResDto {
    private Integer id;
    private String name;
    private String departmentName;
    private String email;
}
