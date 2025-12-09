package com.green.university.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.university.dto.counseling.*;
import com.green.university.repository.*;
import com.green.university.repository.model.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 상담 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AICounselingService {

    private final CounselingRepository counselingRepository;
    private final CounselingAnalysisRepository analysisRepository;
    private final StudentJpaRepository studentRepository;
    private final ProfessorJpaRepository professorRepository;
    private final StaffJpaRepository staffRepository;
    private final AIAnalysisService aiAnalysisService;
    private final DropoutRiskService dropoutRiskService;
    private final ObjectMapper objectMapper;

    /**
     * 상담 등록 및 AI 분석
     */
    @Transactional
    public CounselingResponseDTO createCounseling(CounselingCreateDTO dto) {
        // 1. 상담 기록 저장
        Counseling counseling = Counseling.builder()
                .studentId(dto.getStudentId())
                .counselorId(dto.getCounselorId())
                .counselorType(dto.getCounselorType())
                .title(dto.getTitle())
                .content(dto.getContent())
                .counselingType(dto.getCounselingType())
                .counselingDate(dto.getCounselingDate())
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();

        counseling = counselingRepository.save(counseling);

        // 2. AI 분석 실행
        AIAnalysisResponseDTO aiResult = performAIAnalysis(counseling);

        // 3. 분석 결과 저장
        CounselingAnalysis analysis = saveAnalysisResult(counseling.getId(), aiResult);

        // 4. 통합 위험도 업데이트
        dropoutRiskService.updateCounselingRisk(
                dto.getStudentId(),
                aiResult.getRiskScore()
        );

        // 5. 응답 DTO 생성
        return buildCounselingResponse(counseling, analysis);
    }

    /**
     * AI 분석 실행
     */
    private AIAnalysisResponseDTO performAIAnalysis(Counseling counseling) {
        Student student = studentRepository.findById(counseling.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        // AI 분석 요청 데이터 구성
        AIAnalysisRequestDTO request = AIAnalysisRequestDTO.builder()
                .studentName(student.getName())
                .counselingType(counseling.getCounselingType())
                .content(counseling.getContent())
                .counselingDate(counseling.getCounselingDate())
                .currentSemester(student.getSemester())
                .build();

        // TODO: 학점, 결석 정보 추가 (StuSub, StuSubDetail에서 계산)

        return aiAnalysisService.analyzeCounseling(request);
    }

    /**
     * 분석 결과 저장
     */
    private CounselingAnalysis saveAnalysisResult(Integer counselingId,
                                                  AIAnalysisResponseDTO aiResult) {
        try {
            CounselingAnalysis analysis = CounselingAnalysis.builder()
                    .counselingId(counselingId)
                    .riskScore(aiResult.getRiskScore())
                    .riskLevel(aiResult.getRiskLevel())
                    .mainFactors(objectMapper.writeValueAsString(aiResult.getMainFactors()))
                    .recommendedActions(objectMapper.writeValueAsString(aiResult.getRecommendedActions()))
                    .analysisDate(new Timestamp(System.currentTimeMillis()))
                    .build();

            return analysisRepository.save(analysis);
        } catch (JsonProcessingException e) {
            log.error("분석 결과 JSON 변환 실패", e);
            throw new RuntimeException("분석 결과 저장 실패", e);
        }
    }

    /**
     * 학생별 상담 기록 조회
     */
    @Transactional(readOnly = true)
    public List<CounselingResponseDTO> getStudentCounselings(Integer studentId) {
        List<Counseling> counselings = counselingRepository
                .findByStudentIdOrderByCounselingDateDesc(studentId);

        return counselings.stream()
                .map(this::buildCounselingResponse)
                .collect(Collectors.toList());
    }

    /**
     * 상담자별 상담 기록 조회
     */
    @Transactional(readOnly = true)
    public List<CounselingResponseDTO> getCounselorCounselings(
            Integer counselorId, String counselorType) {
        List<Counseling> counselings = counselingRepository
                .findByCounselorIdAndCounselorTypeOrderByCounselingDateDesc(
                        counselorId, counselorType);

        return counselings.stream()
                .map(this::buildCounselingResponse)
                .collect(Collectors.toList());
    }

    /**
     * 특정 상담 상세 조회
     */
    @Transactional(readOnly = true)
    public CounselingResponseDTO getCounselingDetail(Integer counselingId) {
        Counseling counseling = counselingRepository.findById(counselingId)
                .orElseThrow(() -> new IllegalArgumentException("상담 기록을 찾을 수 없습니다."));

        CounselingAnalysis analysis = analysisRepository
                .findByCounselingId(counselingId)
                .orElse(null);

        return buildCounselingResponse(counseling, analysis);
    }

    /**
     * 고위험 상담 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CounselingResponseDTO> getHighRiskCounselings(Integer minScore) {
        List<CounselingAnalysis> analyses = analysisRepository
                .findHighRiskAnalyses(minScore);

        return analyses.stream()
                .map(analysis -> {
                    Counseling counseling = counselingRepository
                            .findById(analysis.getCounselingId())
                            .orElse(null);
                    return counseling != null ?
                            buildCounselingResponse(counseling, analysis) : null;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    /**
     * 응답 DTO 생성
     */
    private CounselingResponseDTO buildCounselingResponse(Counseling counseling) {
        CounselingAnalysis analysis = analysisRepository
                .findByCounselingId(counseling.getId())
                .orElse(null);
        return buildCounselingResponse(counseling, analysis);
    }

    private CounselingResponseDTO buildCounselingResponse(
            Counseling counseling, CounselingAnalysis analysis) {

        Student student = studentRepository.findById(counseling.getStudentId())
                .orElse(null);

        String counselorName = getCounselorName(
                counseling.getCounselorId(),
                counseling.getCounselorType()
        );

        CounselingAnalysisDTO analysisDTO = null;
        if (analysis != null) {
            analysisDTO = CounselingAnalysisDTO.builder()
                    .id(analysis.getId())
                    .counselingId(analysis.getCounselingId())
                    .riskScore(analysis.getRiskScore())
                    .riskLevel(analysis.getRiskLevel())
                    .mainFactors(parseJsonArray(analysis.getMainFactors()))
                    .recommendedActions(parseJsonArray(analysis.getRecommendedActions()))
                    .analysisDate(analysis.getAnalysisDate())
                    .build();
        }

        return CounselingResponseDTO.builder()
                .id(counseling.getId())
                .studentId(counseling.getStudentId())
                .studentName(student != null ? student.getName() : "알 수 없음")
                .counselorId(counseling.getCounselorId())
                .counselorName(counselorName)
                .counselorType(counseling.getCounselorType())
                .title(counseling.getTitle())
                .content(counseling.getContent())
                .counselingType(counseling.getCounselingType())
                .counselingDate(counseling.getCounselingDate())
                .createdAt(counseling.getCreatedAt())
                .analysis(analysisDTO)
                .build();
    }

    /**
     * 상담자 이름 조회
     */
    private String getCounselorName(Integer counselorId, String counselorType) {
        if ("PROFESSOR".equals(counselorType)) {
            return professorRepository.findById(counselorId)
                    .map(Professor::getName)
                    .orElse("알 수 없음");
        } else if ("STAFF".equals(counselorType)) {
            return staffRepository.findById(counselorId)
                    .map(Staff::getName)
                    .orElse("알 수 없음");
        }
        return "알 수 없음";
    }

    /**
     * JSON 배열 문자열을 List로 변환
     */
    private List<String> parseJsonArray(String jsonStr) {
        try {
            return objectMapper.readValue(jsonStr,
                    objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            log.error("JSON 파싱 실패: {}", jsonStr, e);
            return List.of();
        }
    }
}