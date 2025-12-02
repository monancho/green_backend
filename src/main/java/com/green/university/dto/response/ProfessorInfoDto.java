package com.green.university.dto.response;

import java.sql.Date;

import com.green.university.repository.model.Professor;
import lombok.Data;

@Data
public class ProfessorInfoDto {

    private Integer id;
    private String name;
    private Date birthDate;
    private String gender;
    private String address;
    private String tel;
    private String email;
    private Integer deptId;
    private Date hireDate;
    private String deptName;
    private String collegeName;

    public ProfessorInfoDto(Professor professor) {
        this.id = professor.getId();
        this.name = professor.getName();
        this.birthDate = professor.getBirthDate();
        this.gender = professor.getGender();
        this.address = professor.getAddress();
        this.tel = professor.getTel();
        this.email = professor.getEmail();
        this.hireDate = professor.getHireDate();

        // 연관관계에서 가져오는 값들
        if (professor.getDepartment() != null) {
            this.deptId = professor.getDepartment().getId();
            this.deptName = professor.getDepartment().getName();

            if (professor.getDepartment().getCollege() != null) {
                this.collegeName = professor.getDepartment().getCollege().getName();
            }
        }
    }
}
