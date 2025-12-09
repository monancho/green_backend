package com.green.university.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.university.dto.counseling.AIAnalysisRequestDTO;
import com.green.university.dto.counseling.AIAnalysisResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Google Gemini API를 활용한 AI 분석 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIAnalysisService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";

    /**
     * 상담 내용을 AI로 분석하여 중도이탈 위험도 평가
     */
    public AIAnalysisResponseDTO analyzeCounseling(AIAnalysisRequestDTO request) {
        try {
            String prompt = buildAnalysisPrompt(request);
            String aiResponse = callGeminiAPI(prompt);
            return parseAIResponse(aiResponse);
        } catch (Exception e) {
            log.error("AI 분석 중 오류 발생", e);
            // 실패 시 기본 분석 결과 반환
            return createFallbackAnalysis(request);
        }
    }

    /**
     * AI 프롬프트 생성
     */
    private String buildAnalysisPrompt(AIAnalysisRequestDTO request) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("당신은 대학교 학생 상담 전문가입니다.\n\n");
        prompt.append("아래 학생의 상담 기록을 분석하여 중도이탈(학업 중단) 위험도를 평가해주세요.\n\n");

        prompt.append("### 학생 정보\n");
        prompt.append("- 이름: ").append(request.getStudentName()).append("\n");
        prompt.append("- 현재 학기: ").append(request.getCurrentSemester()).append("\n");

        if (request.getCurrentGPA() != null) {
            prompt.append("- 현재 학점: ").append(request.getCurrentGPA()).append("\n");
        }
        if (request.getTotalAbsences() != null) {
            prompt.append("- 누적 결석: ").append(request.getTotalAbsences()).append("회\n");
        }

        prompt.append("\n### 상담 내용\n");
        prompt.append("- 상담 유형: ").append(request.getCounselingType()).append("\n");
        prompt.append("- 상담 일자: ").append(request.getCounselingDate()).append("\n");
        prompt.append("- 상담 내용:\n").append(request.getContent()).append("\n\n");

        prompt.append("### 분석 요청\n");
        prompt.append("위 상담 내용을 바탕으로 다음 형식의 JSON으로만 응답해주세요. 다른 설명은 절대 포함하지 마세요:\n\n");
        prompt.append("{\n");
        prompt.append("  \"riskScore\": 75,\n");
        prompt.append("  \"riskLevel\": \"HIGH\",\n");
        prompt.append("  \"mainFactors\": [\"경제적 부담\", \"학업 스트레스\", \"과제 미이행\"],\n");
        prompt.append("  \"recommendedActions\": [\"장학금 제도 안내\", \"학업 코칭 프로그램 연계\", \"정기 모니터링\"],\n");
        prompt.append("  \"summary\": \"학생은 경제적 어려움으로 인해 학업에 집중하기 어려운 상황입니다.\"\n");
        prompt.append("}\n\n");

        prompt.append("**중요 규칙:**\n");
        prompt.append("- riskScore: 0~100 사이의 정수\n");
        prompt.append("- riskLevel: LOW, MEDIUM, HIGH 중 하나\n");
        prompt.append("- mainFactors: 정확히 3개의 위험 요인\n");
        prompt.append("- recommendedActions: 정확히 3개의 권장 조치\n");
        prompt.append("- summary: 2-3문장의 요약\n");
        prompt.append("- JSON 형식만 출력하고 다른 텍스트는 포함하지 마세요");

        return prompt.toString();
    }

    /**
     * Gemini API 호출
     */
    private String callGeminiAPI(String prompt) throws Exception {
        String url = GEMINI_API_URL + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Gemini API 요청 구조
        GeminiRequest request = new GeminiRequest(
                List.of(new Content(
                        List.of(new Part(prompt))
                ))
        );

        String requestBody = objectMapper.writeValueAsString(request);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        // Gemini 응답 파싱
        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode candidates = root.get("candidates");

        if (candidates != null && candidates.isArray() && candidates.size() > 0) {
            JsonNode content = candidates.get(0).get("content");
            JsonNode parts = content.get("parts");
            if (parts != null && parts.isArray() && parts.size() > 0) {
                return parts.get(0).get("text").asText();
            }
        }

        throw new RuntimeException("Gemini API 응답 파싱 실패");
    }

    /**
     * AI 응답 파싱
     */
    private AIAnalysisResponseDTO parseAIResponse(String aiResponse) {
        try {
            // JSON 추출 (코드 블록 제거)
            String jsonStr = aiResponse.trim();

            // ```json 또는 ``` 태그 제거
            if (jsonStr.contains("```json")) {
                jsonStr = jsonStr.substring(
                        jsonStr.indexOf("```json") + 7,
                        jsonStr.lastIndexOf("```")
                ).trim();
            } else if (jsonStr.contains("```")) {
                jsonStr = jsonStr.substring(
                        jsonStr.indexOf("```") + 3,
                        jsonStr.lastIndexOf("```")
                ).trim();
            }

            // JSON 파싱 시작 위치 찾기
            int jsonStart = jsonStr.indexOf("{");
            int jsonEnd = jsonStr.lastIndexOf("}");

            if (jsonStart != -1 && jsonEnd != -1) {
                jsonStr = jsonStr.substring(jsonStart, jsonEnd + 1);
            }

            JsonNode json = objectMapper.readTree(jsonStr);

            return AIAnalysisResponseDTO.builder()
                    .riskScore(json.get("riskScore").asInt())
                    .riskLevel(json.get("riskLevel").asText())
                    .mainFactors(parseJsonArray(json.get("mainFactors")))
                    .recommendedActions(parseJsonArray(json.get("recommendedActions")))
                    .summary(json.get("summary").asText())
                    .build();

        } catch (Exception e) {
            log.error("AI 응답 파싱 실패: {}", aiResponse, e);
            throw new RuntimeException("AI 분석 결과 파싱 실패", e);
        }
    }

    /**
     * JSON 배열을 List로 변환
     */
    private List<String> parseJsonArray(JsonNode arrayNode) {
        List<String> result = new ArrayList<>();
        if (arrayNode != null && arrayNode.isArray()) {
            arrayNode.forEach(node -> result.add(node.asText()));
        }
        return result;
    }

    /**
     * API 실패 시 대체 분석 결과 생성
     */
    private AIAnalysisResponseDTO createFallbackAnalysis(AIAnalysisRequestDTO request) {
        int riskScore = calculateBasicRiskScore(request);
        String riskLevel = getRiskLevel(riskScore);

        return AIAnalysisResponseDTO.builder()
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .mainFactors(Arrays.asList("상담 필요", "모니터링 권장", "추가 분석 필요"))
                .recommendedActions(Arrays.asList("담당 교수 면담", "학생 지원 센터 연계", "정기 상담"))
                .summary("자동 분석: 추가 상담이 필요한 학생입니다.")
                .build();
    }

    /**
     * 기본 위험도 점수 계산 (AI 실패 시)
     */
    private int calculateBasicRiskScore(AIAnalysisRequestDTO request) {
        int score = 50;  // 기본 점수

        // 학점 반영
        if (request.getCurrentGPA() != null) {
            if (request.getCurrentGPA() < 2.0) score += 30;
            else if (request.getCurrentGPA() < 3.0) score += 15;
        }

        // 결석 반영
        if (request.getTotalAbsences() != null) {
            if (request.getTotalAbsences() > 10) score += 20;
            else if (request.getTotalAbsences() > 5) score += 10;
        }

        // 상담 유형 반영
        String type = request.getCounselingType();
        if ("정서".equals(type) || "가정".equals(type)) {
            score += 15;
        }

        return Math.min(score, 100);
    }

    /**
     * 위험도 레벨 결정
     */
    private String getRiskLevel(int score) {
        if (score >= 80) return "HIGH";
        if (score >= 50) return "MEDIUM";
        return "LOW";
    }

    // Gemini API 요청/응답 DTO
    record GeminiRequest(List<Content> contents) {}
    record Content(List<Part> parts) {}
    record Part(String text) {}
}