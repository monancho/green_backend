package com.green.university.controller;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import com.green.university.service.AICounselingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.green.university.dto.counseling.*;
import com.green.university.service.CounselingService;
import com.green.university.service.DropoutRiskService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AI 상담 시스템 테스트용 컨트롤러
 * 프론트엔드 없이 브라우저에서 직접 테스트할 수 있습니다.
 */
@Slf4j
@Controller
@RequestMapping("/test/counseling")
@RequiredArgsConstructor
public class AICounselingTestController {

    private final AICounselingService counselingService;
    private final DropoutRiskService dropoutRiskService;

    /**
     * 메인 테스트 페이지
     */
    @GetMapping
    public String testPage(Model model) {
        model.addAttribute("title", "AI 상담 시스템 테스트");
        return "test/counseling-test";
    }

    /**
     * 샘플 상담 데이터 생성 및 AI 분석
     */
    @PostMapping("/create-sample")
    @ResponseBody
    public String createSampleCounseling(
            @RequestParam(defaultValue = "2023000009") Integer studentId,
            @RequestParam(defaultValue = "5") Integer counselorId) {

        try {
            // 샘플 상담 데이터 생성
            CounselingCreateDTO dto = CounselingCreateDTO.builder()
                    .studentId(studentId)
                    .counselorId(counselorId)
                    .counselorType("PROFESSOR")
                    .title("학업 및 경제적 어려움 상담")
                    .content(getSampleContent())
                    .counselingType("학업")
                    .counselingDate(Date.valueOf(LocalDate.now()))
                    .build();

            // 상담 등록 (AI 자동 분석 포함)
            CounselingResponseDTO response = counselingService.createCounseling(dto);

            // 결과 출력
            StringBuilder result = new StringBuilder();
            result.append("✅ 상담 등록 및 AI 분석 완료!\n\n");
            result.append("📋 상담 정보:\n");
            result.append("- ID: ").append(response.getId()).append("\n");
            result.append("- 학생: ").append(response.getStudentName()).append("\n");
            result.append("- 상담자: ").append(response.getCounselorName()).append("\n");
            result.append("- 제목: ").append(response.getTitle()).append("\n\n");

            if (response.getAnalysis() != null) {
                CounselingAnalysisDTO analysis = response.getAnalysis();
                result.append("🤖 AI 분석 결과:\n");
                result.append("- 위험도 점수: ").append(analysis.getRiskScore()).append("/100\n");
                result.append("- 위험 레벨: ").append(analysis.getRiskLevel()).append("\n");
                result.append("- 주요 요인:\n");
                analysis.getMainFactors().forEach(factor ->
                        result.append("  • ").append(factor).append("\n"));
                result.append("- 권장 조치:\n");
                analysis.getRecommendedActions().forEach(action ->
                        result.append("  • ").append(action).append("\n"));
            }

            return result.toString();

        } catch (Exception e) {
            log.error("샘플 상담 생성 실패", e);
            return "❌ 오류 발생: " + e.getMessage();
        }
    }

    /**
     * 고위험 학생 목록 조회
     */
    @GetMapping("/high-risk-students")
    @ResponseBody
    public String getHighRiskStudents(
            @RequestParam(defaultValue = "2025") Integer year,
            @RequestParam(defaultValue = "2") Integer semester,
            @RequestParam(defaultValue = "60") Integer minScore) {

        try {
            List<RiskStudentListDTO> students =
                    dropoutRiskService.getHighRiskStudents(year, semester, minScore);

            if (students.isEmpty()) {
                return "ℹ️ 조건에 맞는 고위험 학생이 없습니다.\n" +
                        "(위험도 " + minScore + "점 이상)";
            }

            StringBuilder result = new StringBuilder();
            result.append("⚠️ 고위험 학생 목록 (").append(students.size()).append("명)\n\n");

            for (RiskStudentListDTO student : students) {
                result.append("━━━━━━━━━━━━━━━━━━━━━━\n");
                result.append("👤 ").append(student.getStudentName()).append("\n");
                result.append("- 학과: ").append(student.getDepartmentName()).append("\n");
                result.append("- 학년: ").append(student.getGrade()).append("학년\n");
                result.append("- 총 위험도: ").append(student.getTotalRiskScore()).append("/100 (")
                        .append(student.getRiskLevel()).append(")\n");
                result.append("  • 상담: ").append(student.getCounselingRiskScore()).append("점\n");
                result.append("  • 성적: ").append(student.getGradeRiskScore()).append("점\n");
                result.append("  • 출석: ").append(student.getAttendanceRiskScore()).append("점\n");

                if (student.getLastCounselingDate() != null) {
                    result.append("- 최근 상담: ").append(student.getLastCounselingDate()).append("\n");
                }
                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("고위험 학생 조회 실패", e);
            return "❌ 오류 발생: " + e.getMessage();
        }
    }

    /**
     * 특정 학생의 위험도 상세 조회
     */
    @GetMapping("/student-risk/{studentId}")
    @ResponseBody
    public String getStudentRisk(
            @PathVariable Integer studentId,
            @RequestParam(defaultValue = "2025") Integer year,
            @RequestParam(defaultValue = "2") Integer semester) {

        try {
            DropoutRiskResponseDTO risk =
                    dropoutRiskService.getStudentRiskDetail(studentId, year, semester);

            StringBuilder result = new StringBuilder();
            result.append("📊 학생 위험도 상세 정보\n\n");
            result.append("👤 학생: ").append(risk.getStudentName()).append("\n");
            result.append("🏫 학과: ").append(risk.getDepartmentName()).append("\n");
            result.append("📅 학기: ").append(risk.getYear()).append("년 ")
                    .append(risk.getSemester()).append("학기\n\n");

            result.append("⚠️ 총 위험도: ").append(risk.getTotalRiskScore())
                    .append("/100 (").append(risk.getRiskLevel()).append(")\n\n");

            result.append("📈 세부 위험도:\n");
            result.append("- 상담 기반: ").append(risk.getCounselingRiskScore()).append("점 (40%)\n");
            result.append("- 성적 기반: ").append(risk.getGradeRiskScore()).append("점 (35%)\n");
            result.append("- 출석 기반: ").append(risk.getAttendanceRiskScore()).append("점 (25%)\n\n");

            if (risk.getLastCounselingDate() != null) {
                result.append("💬 최근 상담:\n");
                result.append("- 일자: ").append(risk.getLastCounselingDate()).append("\n");
                result.append("- 유형: ").append(risk.getLastCounselingType()).append("\n");
            }

            return result.toString();

        } catch (IllegalArgumentException e) {
            return "❌ 해당 학생의 위험도 데이터가 없습니다.";
        } catch (Exception e) {
            log.error("학생 위험도 조회 실패", e);
            return "❌ 오류 발생: " + e.getMessage();
        }
    }

    /**
     * AI 분석만 단독 테스트
     */
    @PostMapping("/test-ai-only")
    @ResponseBody
    public String testAIAnalysis(@RequestParam String content) {
        try {
            AIAnalysisRequestDTO request = AIAnalysisRequestDTO.builder()
                    .studentName("테스트학생")
                    .counselingType("학업")
                    .content(content)
                    .counselingDate(Date.valueOf(LocalDate.now()))
                    .currentSemester(3)
                    .currentGPA(2.5)
                    .totalAbsences(8)
                    .build();

            // AI 분석 서비스 직접 호출이 필요하면 별도 메서드 추가
            return "⚠️ 이 기능은 counselingService에서 분리된 aiAnalysisService가 필요합니다.";

        } catch (Exception e) {
            return "❌ 오류: " + e.getMessage();
        }
    }

    /**
     * 샘플 상담 내용 생성
     */
    private String getSampleContent() {
        return "최근 가정 형편이 어려워져서 아르바이트를 늘려야 할 것 같습니다. " +
                "수업을 자주 빠지게 되고, 과제도 제출하지 못하는 경우가 많아졌습니다. " +
                "전공 수업 내용도 점점 어려워지고 있어서 따라가기 힘듭니다. " +
                "이번 학기를 마치고 휴학을 진지하게 고민하고 있습니다.";
    }
}