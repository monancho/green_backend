package com.green.university.controller;

import java.util.List;

import com.green.university.service.AICounselingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.green.university.dto.counseling.*;
import com.green.university.service.DropoutRiskService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 상담 관리 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/counseling")
@RequiredArgsConstructor
public class AICounselingController {

    private final AICounselingService counselingService;
    private final DropoutRiskService dropoutRiskService;

    /**
     * 상담 등록 (교수, 직원)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('PROFESSOR', 'STAFF')")
    public ResponseEntity<CounselingResponseDTO> createCounseling(
            @RequestBody CounselingCreateDTO dto) {
        try {
            log.info("상담 등록 요청: 학생ID={}, 상담자ID={}",
                    dto.getStudentId(), dto.getCounselorId());

            CounselingResponseDTO response = counselingService.createCounseling(dto);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("상담 등록 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 학생 상담 기록 조회 (학생 본인, 교수, 직원)
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'PROFESSOR', 'STAFF')")
    public ResponseEntity<List<CounselingResponseDTO>> getStudentCounselings(
            @PathVariable Integer studentId) {
        try {
            List<CounselingResponseDTO> counselings =
                    counselingService.getStudentCounselings(studentId);

            return ResponseEntity.ok(counselings);
        } catch (Exception e) {
            log.error("학생 상담 기록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 상담자별 상담 기록 조회 (교수, 직원)
     */
    @GetMapping("/counselor/{counselorId}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'STAFF')")
    public ResponseEntity<List<CounselingResponseDTO>> getCounselorCounselings(
            @PathVariable Integer counselorId,
            @RequestParam String counselorType) {
        try {
            List<CounselingResponseDTO> counselings =
                    counselingService.getCounselorCounselings(counselorId, counselorType);

            return ResponseEntity.ok(counselings);
        } catch (Exception e) {
            log.error("상담자 상담 기록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 상담 상세 조회
     */
    @GetMapping("/{counselingId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'PROFESSOR', 'STAFF')")
    public ResponseEntity<CounselingResponseDTO> getCounselingDetail(
            @PathVariable Integer counselingId) {
        try {
            CounselingResponseDTO counseling =
                    counselingService.getCounselingDetail(counselingId);

            return ResponseEntity.ok(counseling);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("상담 상세 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 고위험 상담 목록 조회 (직원)
     */
    @GetMapping("/high-risk")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<CounselingResponseDTO>> getHighRiskCounselings(
            @RequestParam(defaultValue = "70") Integer minScore) {
        try {
            List<CounselingResponseDTO> counselings =
                    counselingService.getHighRiskCounselings(minScore);

            return ResponseEntity.ok(counselings);
        } catch (Exception e) {
            log.error("고위험 상담 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 통합 위험도 조회 (학생)
     */
    @GetMapping("/risk/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'PROFESSOR', 'STAFF')")
    public ResponseEntity<DropoutRiskResponseDTO> getStudentRisk(
            @PathVariable Integer studentId,
            @RequestParam Integer year,
            @RequestParam Integer semester) {
        try {
            DropoutRiskResponseDTO risk =
                    dropoutRiskService.getStudentRiskDetail(studentId, year, semester);

            return ResponseEntity.ok(risk);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("학생 위험도 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 고위험 학생 목록 조회 (직원)
     */
    @GetMapping("/risk/high-risk-students")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<RiskStudentListDTO>> getHighRiskStudents(
            @RequestParam Integer year,
            @RequestParam Integer semester,
            @RequestParam(defaultValue = "60") Integer minScore) {
        try {
            List<RiskStudentListDTO> students =
                    dropoutRiskService.getHighRiskStudents(year, semester, minScore);

            return ResponseEntity.ok(students);
        } catch (Exception e) {
            log.error("고위험 학생 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 학과별 위험도 통계 (교수, 직원)
     */
    @GetMapping("/risk/statistics/department/{deptId}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'STAFF')")
    public ResponseEntity<RiskStatisticsDTO> getDepartmentStatistics(
            @PathVariable Integer deptId,
            @RequestParam Integer year,
            @RequestParam Integer semester) {
        try {
            RiskStatisticsDTO statistics =
                    dropoutRiskService.getDepartmentRiskStatistics(deptId, year, semester);

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("학과 통계 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 성적 기반 위험도 수동 업데이트 (테스트용)
     */
    @PostMapping("/risk/update-grade")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> updateGradeRisk(
            @RequestParam Integer studentId,
            @RequestParam Integer year,
            @RequestParam Integer semester) {
        try {
            dropoutRiskService.updateGradeRisk(studentId, year, semester);
            return ResponseEntity.ok("성적 위험도 업데이트 완료");
        } catch (Exception e) {
            log.error("성적 위험도 업데이트 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("업데이트 실패: " + e.getMessage());
        }
    }

    /**
     * 출석 기반 위험도 수동 업데이트 (테스트용)
     */
    @PostMapping("/risk/update-attendance")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> updateAttendanceRisk(
            @RequestParam Integer studentId,
            @RequestParam Integer year,
            @RequestParam Integer semester) {
        try {
            dropoutRiskService.updateAttendanceRisk(studentId, year, semester);
            return ResponseEntity.ok("출석 위험도 업데이트 완료");
        } catch (Exception e) {
            log.error("출석 위험도 업데이트 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("업데이트 실패: " + e.getMessage());
        }
    }
}