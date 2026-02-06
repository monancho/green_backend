package com.green.university.controller;

import com.green.university.service.DailyRiskAnalysisScheduler;
import com.green.university.service.MeetingReminderScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 테스트용 컨트롤러 (개발 환경에서만 사용)
 * 스케줄러를 수동으로 실행하여 테스트할 수 있도록 함
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final DailyRiskAnalysisScheduler dailyRiskAnalysisScheduler;
    private final MeetingReminderScheduler meetingReminderScheduler;

    /**
     * 매일 자동 위험도 분석 스케줄러 수동 실행
     * POST /api/test/daily-analysis
     */
    @PostMapping("/daily-analysis")
    public ResponseEntity<Map<String, String>> testDailyAnalysis() {
        try {
            dailyRiskAnalysisScheduler.analyzeAllStudentsDaily();
            Map<String, String> response = new HashMap<>();
            response.put("message", "매일 자동 위험도 분석이 실행되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("테스트 실행 중 오류 발생", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "오류 발생: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 회의 시작 10분 전 알림 스케줄러 수동 실행
     * POST /api/test/meeting-reminder
     */
    @PostMapping("/meeting-reminder")
    public ResponseEntity<Map<String, String>> testMeetingReminder() {
        try {
            meetingReminderScheduler.sendMeetingReminders();
            Map<String, String> response = new HashMap<>();
            response.put("message", "회의 시작 10분 전 알림 스케줄러가 실행되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("테스트 실행 중 오류 발생", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "오류 발생: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}

