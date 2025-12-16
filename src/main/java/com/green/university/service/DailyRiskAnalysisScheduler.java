package com.green.university.service;

import com.green.university.utils.Define;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 매일 자동으로 학생 위험도 분석을 실행하는 스케줄러
 */
@Slf4j
@Service
public class DailyRiskAnalysisScheduler {

    @Autowired
    private AIAnalysisResultService aiAnalysisResultService;

    /**
     * 매일 오전 9시에 실행하여 모든 학생의 위험도를 분석
     * cron = "0 0 9 * * ?" -> 매일 오전 9시 정각
     */
    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void analyzeAllStudentsDaily() {
        try {
            log.info("매일 자동 위험도 분석 시작");

            // 현재 연도와 학기 가져오기
            int currentYear = Define.getCurrentYear();
            int currentSemester = Define.getCurrentSemester();

            // 전체 학생-과목에 대한 분석 실행
            int successCount = aiAnalysisResultService.analyzeAllStudentsAndSubjects(
                    currentYear,
                    currentSemester
            );

            log.info("매일 자동 위험도 분석 완료: {}건 분석 성공", successCount);
        } catch (Exception e) {
            log.error("매일 자동 위험도 분석 중 오류 발생", e);
        }
    }
}

