package com.green.university.repository.model;

import java.sql.Date;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 상담 기록을 나타내는 JPA 엔티티
 * 학생과 상담자(교수 또는 직원) 간의 상담 내역을 관리합니다.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "counseling_tb")
public class Counseling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * 상담을 받은 학생 ID
     */
    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    /**
     * 상담을 받은 학생 (읽기 전용)
     */
    @ManyToOne
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private Student student;

    /**
     * 상담자 ID (교수 또는 직원)
     */
    @Column(name = "counselor_id", nullable = false)
    private Integer counselorId;

    /**
     * 상담자 타입: PROFESSOR, STAFF
     */
    @Column(name = "counselor_type", nullable = false)
    private String counselorType;

    /**
     * 상담 제목
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 상담 내용 (AI 분석 대상)
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 상담 유형: 학업, 정서, 가정, 경력, 기타
     */
    @Column(name = "counseling_type", nullable = false)
    private String counselingType;

    /**
     * 상담 일자
     */
    @Column(name = "counseling_date", nullable = false)
    private Date counselingDate;

    /**
     * 등록 시간
     */
    @Column(name = "created_at")
    private Timestamp createdAt;
}