package com.green.university.repository;

import com.green.university.repository.model.DropoutRisk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 통합 위험도 Repository
 */
@Repository
public interface DropoutRiskRepository extends JpaRepository<DropoutRisk, Integer> {

    // 특정 학생의 특정 학기 위험도 조회
    Optional<DropoutRisk> findByStudentIdAndYearAndSemester(
            Integer studentId, Integer year, Integer semester);

    // 특정 학생의 모든 위험도 기록 조회
    List<DropoutRisk> findByStudentIdOrderByYearDescSemesterDesc(Integer studentId);

    // 위험 레벨별 학생 목록 조회
    List<DropoutRisk> findByYearAndSemesterAndRiskLevel(
            Integer year, Integer semester, String riskLevel);

    // 현재 학기 고위험 학생 목록 조회
    @Query("SELECT dr FROM DropoutRisk dr " +
            "WHERE dr.year = :year AND dr.semester = :semester " +
            "AND dr.totalRiskScore >= :minScore " +
            "ORDER BY dr.totalRiskScore DESC")
    List<DropoutRisk> findHighRiskStudents(
            @Param("year") Integer year,
            @Param("semester") Integer semester,
            @Param("minScore") Integer minScore);

    // 특정 학과의 위험도 통계
    @Query("SELECT dr FROM DropoutRisk dr " +
            "JOIN dr.student s " +
            "WHERE s.deptId = :deptId " +
            "AND dr.year = :year AND dr.semester = :semester " +
            "ORDER BY dr.totalRiskScore DESC")
    List<DropoutRisk> findByDepartmentAndSemester(
            @Param("deptId") Integer deptId,
            @Param("year") Integer year,
            @Param("semester") Integer semester);
}