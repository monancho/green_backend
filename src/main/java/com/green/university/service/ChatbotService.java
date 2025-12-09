package com.green.university.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.green.university.dto.response.MyGradeDto;
import com.green.university.repository.model.*;
import com.green.university.utils.Define;

/**
 * 챗봇 서비스
 * 학생의 등록 여부, 수강 신청, 취득 학점, 졸업 요건 등을 조회하여 답변을 생성합니다.
 */
@Service
public class ChatbotService {

    @Autowired
    private UserService userService;

    @Autowired
    private TuitionService tuitionService;

    @Autowired
    private StuSubService stuSubService;

    @Autowired
    private GradeService gradeService;

    @Autowired
    private StuStatService stuStatService;

    /**
     * 사용자 메시지를 분석하고 적절한 답변을 생성합니다.
     */
    @Transactional(readOnly = true)
    public String processMessage(Integer studentId, String message) {
        if (message == null || message.trim().isEmpty()) {
            return "안녕하세요! 무엇을 도와드릴까요?";
        }

        String lowerMessage = message.toLowerCase().trim();

        // 등록 여부 관련 키워드
        if (containsAny(lowerMessage, "등록", "등록금", "납부", "납입", "등록여부", "등록상태")) {
            return getRegistrationStatus(studentId);
        }

        // 수강 신청 관련 키워드
        if (containsAny(lowerMessage, "수강", "수강신청", "신청", "과목", "강의", "수강내역", "수강목록")) {
            return getCourseEnrollmentInfo(studentId);
        }

        // 학점 관련 키워드
        if (containsAny(lowerMessage, "학점", "취득학점", "이수학점", "평점", "성적", "학점평균", "평균")) {
            return getGradeInfo(studentId);
        }

        // 졸업 요건 관련 키워드
        if (containsAny(lowerMessage, "졸업", "졸업요건", "졸업조건", "졸업학점", "졸업가능")) {
            return getGraduationRequirements(studentId);
        }

        // 인사말
        if (containsAny(lowerMessage, "안녕", "하이", "hello", "hi", "반가워", "처음")) {
            return "안녕하세요! 그린대학교 챗봇입니다. 등록 여부, 수강 신청, 학점, 졸업 요건 등에 대해 물어보실 수 있습니다.";
        }

        // 도움말
        if (containsAny(lowerMessage, "도움", "도와", "help", "무엇", "뭐", "어떻게", "기능")) {
            return getHelpMessage();
        }

        // 기본 응답
        return "죄송합니다. 질문을 이해하지 못했습니다. 다음 중 하나를 물어보세요:\n" +
               "• 등록 여부\n" +
               "• 수강 신청 내역\n" +
               "• 취득 학점\n" +
               "• 졸업 요건";
    }

    /**
     * 등록 여부 조회
     */
    private String getRegistrationStatus(Integer studentId) {
        try {
            Tuition tuition = tuitionService.readByStudentIdAndSemester(
                    studentId, Define.CURRENT_YEAR, Define.CURRENT_SEMESTER);

            if (tuition == null) {
                return String.format("%d년 %d학기 등록금 고지서가 아직 발급되지 않았습니다.", 
                        Define.CURRENT_YEAR, Define.CURRENT_SEMESTER);
            }

            Student student = userService.readStudent(studentId);
            StuStat stuStat = stuStatService.readCurrentStatus(studentId);

            StringBuilder response = new StringBuilder();
            response.append(String.format("【%d년 %d학기 등록 현황】\n", 
                    Define.CURRENT_YEAR, Define.CURRENT_SEMESTER));
            response.append(String.format("학번: %d\n", studentId));
            response.append(String.format("이름: %s\n", student.getName()));
            response.append(String.format("학적 상태: %s\n", stuStat.getStatus()));

            if (tuition.getStatus() != null && tuition.getStatus()) {
                response.append("등록 상태: ✅ 등록 완료\n");
                response.append(String.format("등록금액: %s원\n", formatNumber(tuition.getTuiAmount())));
                if (tuition.getSchAmount() != null && tuition.getSchAmount() > 0) {
                    response.append(String.format("장학금액: %s원\n", formatNumber(tuition.getSchAmount())));
                }
            } else {
                response.append("등록 상태: ❌ 미등록\n");
                response.append(String.format("등록금액: %s원\n", formatNumber(tuition.getTuiAmount())));
                if (tuition.getSchAmount() != null && tuition.getSchAmount() > 0) {
                    response.append(String.format("장학금액: %s원\n", formatNumber(tuition.getSchAmount())));
                    int paymentAmount = tuition.getTuiAmount() - tuition.getSchAmount();
                    response.append(String.format("납부금액: %s원\n", formatNumber(paymentAmount)));
                } else {
                    response.append(String.format("납부금액: %s원\n", formatNumber(tuition.getTuiAmount())));
                }
                response.append("\n등록금 납부 페이지에서 납부하실 수 있습니다.");
            }

            return response.toString();
        } catch (Exception e) {
            return "등록 정보를 조회하는 중 오류가 발생했습니다.";
        }
    }

    /**
     * 수강 신청 내역 조회
     */
    private String getCourseEnrollmentInfo(Integer studentId) {
        try {
            List<StuSub> stuSubList = stuSubService.readStuSubList(studentId);

            if (stuSubList.isEmpty()) {
                return String.format("%d년 %d학기 수강 신청 내역이 없습니다.", 
                        Define.CURRENT_YEAR, Define.CURRENT_SEMESTER);
            }

            StringBuilder response = new StringBuilder();
            response.append(String.format("【%d년 %d학기 수강 신청 내역】\n\n", 
                    Define.CURRENT_YEAR, Define.CURRENT_SEMESTER));

            int totalCredits = 0;
            for (StuSub stuSub : stuSubList) {
                Subject subject = stuSub.getSubject();
                if (subject != null) {
                    response.append(String.format("• %s (%s학점)\n", 
                            subject.getName(), subject.getGrades()));
                    totalCredits += subject.getGrades();
                }
            }

            response.append(String.format("\n총 신청 학점: %d학점", totalCredits));

            return response.toString();
        } catch (Exception e) {
            return "수강 신청 내역을 조회하는 중 오류가 발생했습니다.";
        }
    }

    /**
     * 학점 정보 조회
     */
    private String getGradeInfo(Integer studentId) {
        try {
            // 현재 학기 성적
            MyGradeDto currentSemester = gradeService.readMyGradeByStudentId(studentId);
            
            // 전체 누계 성적
            List<MyGradeDto> totalGrades = gradeService.readgradeinquiryList(studentId);

            StringBuilder response = new StringBuilder();
            response.append("【학점 정보】\n\n");

            // 현재 학기
            if (currentSemester != null && currentSemester.getSumGrades() > 0) {
                response.append(String.format("【%d년 %d학기】\n", 
                        Define.CURRENT_YEAR, Define.CURRENT_SEMESTER));
                response.append(String.format("신청 학점: %d학점\n", currentSemester.getSumGrades()));
                response.append(String.format("취득 학점: %d학점\n", currentSemester.getMyGrades()));
                if (currentSemester.getAverage() > 0) {
                    response.append(String.format("평점 평균: %.2f\n", currentSemester.getAverage()));
                }
                response.append("\n");
            }

            // 전체 누계
            if (totalGrades != null && !totalGrades.isEmpty()) {
                int totalSumGrades = totalGrades.stream()
                        .mapToInt(MyGradeDto::getSumGrades)
                        .sum();
                int totalMyGrades = totalGrades.stream()
                        .mapToInt(MyGradeDto::getMyGrades)
                        .sum();

                // 전체 평균 계산
                double totalAvg = totalGrades.stream()
                        .filter(g -> g.getAverage() > 0)
                        .mapToDouble(MyGradeDto::getAverage)
                        .average()
                        .orElse(0.0);

                response.append("【전체 누계】\n");
                response.append(String.format("총 신청 학점: %d학점\n", totalSumGrades));
                response.append(String.format("총 취득 학점: %d학점\n", totalMyGrades));
                if (totalAvg > 0) {
                    response.append(String.format("전체 평점 평균: %.2f", totalAvg));
                }
            } else {
                response.append("아직 수강한 과목이 없습니다.");
            }

            return response.toString();
        } catch (Exception e) {
            return "학점 정보를 조회하는 중 오류가 발생했습니다.";
        }
    }

    /**
     * 졸업 요건 조회
     */
    private String getGraduationRequirements(Integer studentId) {
        try {
            List<MyGradeDto> totalGrades = gradeService.readgradeinquiryList(studentId);
            
            int totalMyGrades = 0;
            if (totalGrades != null && !totalGrades.isEmpty()) {
                totalMyGrades = totalGrades.stream()
                        .mapToInt(MyGradeDto::getMyGrades)
                        .sum();
            }

            // 기본 졸업 요건 (일반적으로 130학점 이상)
            int requiredCredits = 130;
            int remainingCredits = Math.max(0, requiredCredits - totalMyGrades);

            StringBuilder response = new StringBuilder();
            response.append("【졸업 요건】\n\n");
            response.append(String.format("졸업 필요 학점: %d학점\n", requiredCredits));
            response.append(String.format("현재 취득 학점: %d학점\n", totalMyGrades));
            response.append(String.format("부족한 학점: %d학점\n\n", remainingCredits));

            if (remainingCredits == 0) {
                response.append("✅ 졸업 요건을 충족하셨습니다!");
            } else {
                response.append(String.format("⚠️ 졸업까지 %d학점이 더 필요합니다.", remainingCredits));
            }

            // 추가 정보
            response.append("\n\n※ 참고사항:");
            response.append("\n- 평점 평균 2.0 이상 필요");
            response.append("\n- 전공 필수 과목 이수 확인 필요");
            response.append("\n- 자세한 졸업 요건은 학과 사무실에 문의하세요.");

            return response.toString();
        } catch (Exception e) {
            return "졸업 요건을 조회하는 중 오류가 발생했습니다.";
        }
    }

    /**
     * 도움말 메시지
     */
    private String getHelpMessage() {
        return "【챗봇 사용 안내】\n\n" +
               "다음과 같은 질문을 하실 수 있습니다:\n\n" +
               "📋 등록 관련:\n" +
               "  • 등록 여부 확인\n" +
               "  • 등록금 납부 현황\n\n" +
               "📚 수강 신청 관련:\n" +
               "  • 수강 신청 내역\n" +
               "  • 현재 학기 수강 과목\n\n" +
               "📊 학점 관련:\n" +
               "  • 취득 학점 조회\n" +
               "  • 평점 평균 확인\n\n" +
               "🎓 졸업 관련:\n" +
               "  • 졸업 요건 확인\n" +
               "  • 졸업 가능 여부\n\n" +
               "원하시는 내용을 자유롭게 질문해주세요!";
    }

    /**
     * 문자열에 키워드가 포함되어 있는지 확인
     */
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 숫자를 천 단위 구분자로 포맷팅
     */
    private String formatNumber(Integer number) {
        if (number == null) return "0";
        return String.format("%,d", number);
    }
}

