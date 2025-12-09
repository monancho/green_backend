package com.green.university.repository;

import com.green.university.repository.model.CounselingAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AI 분석 결과 Repository
 */
@Repository
public interface CounselingAnalysisRepository extends JpaRepository<CounselingAnalysis, Integer> {

    // 특정 상담에 대한 분석 결과 조회
    Optional<CounselingAnalysis> findByCounselingId(Integer counselingId);

    // 특정 학생의 모든 분석 결과 조회
    @Query("SELECT ca FROM CounselingAnalysis ca " +
            "JOIN ca.counseling c " +
            "WHERE c.studentId = :studentId " +
            "ORDER BY ca.analysisDate DESC")
    List<CounselingAnalysis> findByStudentId(@Param("studentId") Integer studentId);

    // 위험도 레벨별 분석 결과 조회
    List<CounselingAnalysis> findByRiskLevelOrderByAnalysisDateDesc(String riskLevel);

    // 특정 점수 이상의 고위험 분석 결과 조회
    @Query("SELECT ca FROM CounselingAnalysis ca " +
            "WHERE ca.riskScore >= :minScore " +
            "ORDER BY ca.riskScore DESC, ca.analysisDate DESC")
    List<CounselingAnalysis> findHighRiskAnalyses(@Param("minScore") Integer minScore);
}