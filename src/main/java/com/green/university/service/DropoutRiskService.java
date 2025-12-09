package com.green.university.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.green.university.dto.counseling.DropoutRiskResponseDTO;
import com.green.university.dto.counseling.RiskStatisticsDTO;
import com.green.university.dto.counseling.RiskStudentListDTO;
import com.green.university.repository.*;
import com.green.university.repository.model.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 통합 중도이탈 위험도 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DropoutRiskService {

    private final DropoutRiskRepository dropoutRiskRepository;
    private final StudentJpaRepository studentRepository;
    private final DepartmentJpaRepository departmentRepository;
    private final CounselingRepository counselingRepository;
    private final StuSubJpaRepository stuSubRepository;
    private final StuSubDetailJpaRepository stuSubDetailRepository;

    /**
     * 상담 기반 위험도 업데이트
     */
    @Transactional
    public void updateCounselingRisk(Integer studentId, Integer counselingRiskScore) {
        int currentYear = LocalDate.now().getYear();
        int currentSemester = LocalDate.now().getMonthValue() <= 6 ? 1 : 2;

        DropoutRisk risk = dropoutRiskRepository
                .findByStudentIdAndYearAndSemester(studentId, currentYear, currentSemester)
                .orElse(DropoutRisk.builder()
                        .studentId(studentId)
                        .year(currentYear)
                        .semester(currentSemester)
                        .gradeRiskScore(0)      // 기본값 명시
                        .attendanceRiskScore(0)  // 기본값 명시
                        .counselingRiskScore(0)  // 기본값 명시
                        .build());

        risk.setCounselingRiskScore(counselingRiskScore);
        risk.calculateTotalRiskScore();
        risk.setLastUpdated(new Timestamp(System.currentTimeMillis()));

        dropoutRiskRepository.save(risk);

        log.info("학생 {} 상담 위험도 업데이트: {} -> 총합: {}",
                studentId, counselingRiskScore, risk.getTotalRiskScore());
    }

    /**
     * 성적 기반 위험도 계산 및 업데이트
     */
    @Transactional
    public void updateGradeRisk(Integer studentId, Integer year, Integer semester) {
        Integer gradeRiskScore = calculateGradeRiskScore(studentId, year, semester);

        DropoutRisk risk = dropoutRiskRepository
                .findByStudentIdAndYearAndSemester(studentId, year, semester)
                .orElse(DropoutRisk.builder()
                        .studentId(studentId)
                        .year(year)
                        .semester(semester)
                        .build());

        risk.setGradeRiskScore(gradeRiskScore);
        risk.calculateTotalRiskScore();
        risk.setLastUpdated(new Timestamp(System.currentTimeMillis()));

        dropoutRiskRepository.save(risk);
    }

    /**
     * 출석 기반 위험도 계산 및 업데이트
     */
    @Transactional
    public void updateAttendanceRisk(Integer studentId, Integer year, Integer semester) {
        Integer attendanceRiskScore = calculateAttendanceRiskScore(studentId, year, semester);

        DropoutRisk risk = dropoutRiskRepository
                .findByStudentIdAndYearAndSemester(studentId, year, semester)
                .orElse(DropoutRisk.builder()
                        .studentId(studentId)
                        .year(year)
                        .semester(semester)
                        .build());

        risk.setAttendanceRiskScore(attendanceRiskScore);
        risk.calculateTotalRiskScore();
        risk.setLastUpdated(new Timestamp(System.currentTimeMillis()));

        dropoutRiskRepository.save(risk);
    }

    /**
     * 성적 위험도 계산 로직
     */
    private Integer calculateGradeRiskScore(Integer studentId, Integer year, Integer semester) {
        // TODO: StuSub 데이터에서 해당 학기 성적 조회
        // 평균 학점(GPA) 기반 위험도 계산

        // 임시 계산 로직
        List<StuSub> stuSubs = stuSubRepository.findByStudentId(studentId);

        if (stuSubs.isEmpty()) {
            return 0;
        }

        // F학점 개수 계산
        long fCount = stuSubs.stream()
                .filter(ss -> "F".equals(ss.getGrade()))
                .count();

        // 평균 학점 계산 (간단 버전)
        double avgGrade = stuSubs.stream()
                .filter(ss -> ss.getGrade() != null)
                .mapToInt(this::gradeToScore)
                .average()
                .orElse(0.0);

        int riskScore = 0;

        if (avgGrade < 2.0) riskScore += 40;
        else if (avgGrade < 2.5) riskScore += 25;
        else if (avgGrade < 3.0) riskScore += 15;

        riskScore += (int) (fCount * 10);

        return Math.min(riskScore, 100);
    }

    /**
     * 출석 위험도 계산 로직
     */
    private Integer calculateAttendanceRiskScore(Integer studentId, Integer year, Integer semester) {
        // TODO: StuSubDetail에서 결석, 지각 데이터 조회

        // 임시 계산 로직
        List<StuSubDetail> details = stuSubDetailRepository.findByStudentId(studentId);

        if (details.isEmpty()) {
            return 0;
        }

        int totalAbsent = details.stream()
                .mapToInt(d -> d.getAbsent() != null ? d.getAbsent() : 0)
                .sum();

        int totalLateness = details.stream()
                .mapToInt(d -> d.getLateness() != null ? d.getLateness() : 0)
                .sum();

        int riskScore = 0;

        if (totalAbsent > 15) riskScore += 50;
        else if (totalAbsent > 10) riskScore += 30;
        else if (totalAbsent > 5) riskScore += 15;

        if (totalLateness > 10) riskScore += 20;
        else if (totalLateness > 5) riskScore += 10;

        return Math.min(riskScore, 100);
    }

    /**
     * 학점을 점수로 변환
     */
    private int gradeToScore(StuSub stuSub) {
        String grade = stuSub.getGrade();
        if (grade == null) return 0;

        return switch (grade) {
            case "A+", "A" -> 4;
            case "B+", "B" -> 3;
            case "C+", "C" -> 2;
            case "D+", "D" -> 1;
            default -> 0;
        };
    }

    /**
     * 고위험 학생 목록 조회
     */
    @Transactional(readOnly = true)
    public List<RiskStudentListDTO> getHighRiskStudents(
            Integer year, Integer semester, Integer minScore) {

        List<DropoutRisk> risks = dropoutRiskRepository
                .findHighRiskStudents(year, semester, minScore);

        return risks.stream()
                .map(this::toRiskStudentListDTO)
                .collect(Collectors.toList());
    }

    /**
     * 학과별 위험도 통계
     */
    @Transactional(readOnly = true)
    public RiskStatisticsDTO getDepartmentRiskStatistics(
            Integer deptId, Integer year, Integer semester) {

        List<DropoutRisk> risks = dropoutRiskRepository
                .findByDepartmentAndSemester(deptId, year, semester);

        int total = risks.size();
        long lowCount = risks.stream().filter(r -> "LOW".equals(r.getRiskLevel())).count();
        long mediumCount = risks.stream().filter(r -> "MEDIUM".equals(r.getRiskLevel())).count();
        long highCount = risks.stream().filter(r -> "HIGH".equals(r.getRiskLevel())).count();
        long criticalCount = risks.stream().filter(r -> "CRITICAL".equals(r.getRiskLevel())).count();

        double avgScore = risks.stream()
                .mapToInt(DropoutRisk::getTotalRiskScore)
                .average()
                .orElse(0.0);

        List<RiskStudentListDTO> topRiskStudents = risks.stream()
                .sorted((r1, r2) -> r2.getTotalRiskScore().compareTo(r1.getTotalRiskScore()))
                .limit(10)
                .map(this::toRiskStudentListDTO)
                .collect(Collectors.toList());

        return RiskStatisticsDTO.builder()
                .totalStudents(total)
                .lowRiskCount((int) lowCount)
                .mediumRiskCount((int) mediumCount)
                .highRiskCount((int) highCount)
                .criticalRiskCount((int) criticalCount)
                .averageRiskScore(avgScore)
                .topRiskStudents(topRiskStudents)
                .build();
    }

    /**
     * 학생 위험도 상세 조회
     */
    @Transactional(readOnly = true)
    public DropoutRiskResponseDTO getStudentRiskDetail(
            Integer studentId, Integer year, Integer semester) {

        DropoutRisk risk = dropoutRiskRepository
                .findByStudentIdAndYearAndSemester(studentId, year, semester)
                .orElseThrow(() -> new IllegalArgumentException("위험도 데이터가 없습니다."));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        Department dept = departmentRepository.findById(student.getDeptId())
                .orElse(null);

        Counseling lastCounseling = counselingRepository
                .findFirstByStudentIdOrderByCounselingDateDesc(studentId)
                .orElse(null);

        return DropoutRiskResponseDTO.builder()
                .id(risk.getId())
                .studentId(studentId)
                .studentName(student.getName())
                .departmentName(dept != null ? dept.getName() : "알 수 없음")
                .year(year)
                .semester(semester)
                .gradeRiskScore(risk.getGradeRiskScore())
                .attendanceRiskScore(risk.getAttendanceRiskScore())
                .counselingRiskScore(risk.getCounselingRiskScore())
                .totalRiskScore(risk.getTotalRiskScore())
                .riskLevel(risk.getRiskLevel())
                .lastUpdated(risk.getLastUpdated())
                .lastCounselingDate(lastCounseling != null ?
                        lastCounseling.getCounselingDate() : null)
                .lastCounselingType(lastCounseling != null ?
                        lastCounseling.getCounselingType() : null)
                .build();
    }

    /**
     * DropoutRisk -> RiskStudentListDTO 변환
     */
    private RiskStudentListDTO toRiskStudentListDTO(DropoutRisk risk) {
        Student student = studentRepository.findById(risk.getStudentId())
                .orElse(null);

        Department dept = student != null && student.getDeptId() != null ?
                departmentRepository.findById(student.getDeptId()).orElse(null) : null;

        Counseling lastCounseling = counselingRepository
                .findFirstByStudentIdOrderByCounselingDateDesc(risk.getStudentId())
                .orElse(null);

        return RiskStudentListDTO.builder()
                .studentId(risk.getStudentId())
                .studentName(student != null ? student.getName() : "알 수 없음")
                .departmentName(dept != null ? dept.getName() : "알 수 없음")
                .grade(student != null ? student.getGrade() : null)
                .totalRiskScore(risk.getTotalRiskScore())
                .riskLevel(risk.getRiskLevel())
                .counselingRiskScore(risk.getCounselingRiskScore())
                .gradeRiskScore(risk.getGradeRiskScore())
                .attendanceRiskScore(risk.getAttendanceRiskScore())
                .lastCounselingDate(lastCounseling != null ?
                        lastCounseling.getCounselingDate() : null)
                .lastUpdated(risk.getLastUpdated())
                .build();
    }
}