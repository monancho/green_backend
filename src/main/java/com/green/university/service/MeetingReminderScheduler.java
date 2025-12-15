package com.green.university.service;

import com.green.university.enums.CounselingReservationStatus;
import com.green.university.repository.CounselingReservationJpaRepository;
import com.green.university.repository.NotificationJpaRepository;
import com.green.university.repository.model.CounselingReservation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 회의 시작 10분 전 알림 스케줄러
 */
@Slf4j
@Service
public class MeetingReminderScheduler {

    @Autowired
    private CounselingReservationJpaRepository reservationRepo;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationJpaRepository notificationRepo;

    /**
     * 매 5분마다 실행하여 회의 시작 10분 전 알림을 발송
     */
    @Scheduled(fixedRate = 300000) // 5분마다 실행 (300000ms = 5분)
    @Transactional
    public void sendMeetingReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tenMinutesLater = now.plusMinutes(10);
            
            // 10분 후에 시작하는 회의를 찾기 위한 시간 범위 (5분 간격)
            Timestamp startTime = Timestamp.valueOf(now.plusMinutes(10));
            Timestamp endTime = Timestamp.valueOf(now.plusMinutes(15));

            // 승인된 예약 중에서 시작 시간이 10분 후인 예약들 조회
            List<CounselingReservation> upcomingReservations = reservationRepo
                    .findByStatusAndSlot_StartAtBetween(
                            CounselingReservationStatus.APPROVED,
                            startTime,
                            endTime
                    );

            for (CounselingReservation reservation : upcomingReservations) {
                Long reservationId = reservation.getId();
                
                // 이미 10분 전 알림을 보냈는지 확인 (중복 방지)
                boolean alreadyNotified = notificationRepo.existsByReservationIdAndType(
                        reservationId,
                        "MEETING_REMINDER_10MIN"
                );

                if (!alreadyNotified) {
                    // 교수에게 알림
                    String professorMessage = String.format(
                            "%s 학생과의 상담이 10분 후에 시작됩니다. (%s)",
                            reservation.getStudent().getName(),
                            reservation.getSlot().getStartAt().toLocalDateTime()
                                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    );
                    notificationService.createNotification(
                            reservation.getSlot().getProfessor().getId(),
                            "MEETING_REMINDER_10MIN",
                            professorMessage,
                            reservationId
                    );

                    // 학생에게 알림
                    String studentMessage = String.format(
                            "%s 교수님과의 상담이 10분 후에 시작됩니다. (%s)",
                            reservation.getSlot().getProfessor().getName(),
                            reservation.getSlot().getStartAt().toLocalDateTime()
                                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    );
                    notificationService.createNotification(
                            reservation.getStudent().getId(),
                            "MEETING_REMINDER_10MIN",
                            studentMessage,
                            reservationId
                    );

                    log.info("회의 시작 10분 전 알림 발송: 예약 ID={}, 교수={}, 학생={}",
                            reservationId,
                            reservation.getSlot().getProfessor().getName(),
                            reservation.getStudent().getName());
                }
            }
        } catch (Exception e) {
            log.error("회의 시작 10분 전 알림 발송 중 오류 발생", e);
        }
    }
}


