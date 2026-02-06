package com.green.university.dto.response;

import java.sql.Date;

import com.green.university.repository.model.Student;
import lombok.Data;

@Data
public class StudentInfoDto {

    private Integer id;
    private String name;
    private Date birthDate;
    private String gender;
    private String address;
    private String tel;
    private String email;
    private Integer deptId;
    private Integer grade;
    private Integer semester;
    private Date entranceDate;
    private Date graduationDate;
    private String deptName;
    private String collegeName;

    public StudentInfoDto(Student student) {
        this.id = student.getId();
        this.name = student.getName();
        this.birthDate = student.getBirthDate();
        this.gender = student.getGender();
        this.address = student.getAddress();
        this.tel = student.getTel();
        this.email = student.getEmail();
        this.grade = student.getGrade();
        this.semester = student.getSemester();
        this.entranceDate = student.getEntranceDate();
        this.graduationDate = student.getGraduationDate();

        // 연관관계에서 가져오는 값
        if (student.getDepartment() != null) {
            this.deptId = student.getDepartment().getId();
            this.deptName = student.getDepartment().getName();

            if (student.getDepartment().getCollege() != null) {
                this.collegeName = student.getDepartment().getCollege().getName();
            }
        }
    }
}
