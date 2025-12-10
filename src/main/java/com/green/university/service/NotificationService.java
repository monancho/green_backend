package com.green.university.service;

import com.green.university.dto.response.NotificationResDto;
import com.green.university.dto.response.PrincipalDto;
import com.green.university.repository.NotificationJpaRepository;
import com.green.university.repository.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 알림 서비스
 */
@Service
public class NotificationService {

    @Autowired
    private NotificationJpaRepository notificationRepo;

    /**
     * 알림 생성
     */
    @Transactional
    public Notification createNotification(
            Integer userId,
            String type,
            String message,
            Long reservationId
    ) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setMessage(message);
        notification.setReservationId(reservationId);
        notification.setIsRead(false);
        notification.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        return notificationRepo.save(notification);
    }

    /**
     * 사용자의 읽지 않은 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public List<NotificationResDto> getUnreadNotifications(PrincipalDto principal) {
        List<Notification> notifications = notificationRepo.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(principal.getId());
        return notifications.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 모든 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public List<NotificationResDto> getAllNotifications(PrincipalDto principal) {
        List<Notification> notifications = notificationRepo.findByUserIdOrderByCreatedAtDesc(principal.getId());
        return notifications.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(PrincipalDto principal) {
        return notificationRepo.countByUserIdAndIsReadFalse(principal.getId());
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationId, PrincipalDto principal) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다."));

        // 본인 알림만 읽음 처리 가능
        if (!notification.getUserId().equals(principal.getId())) {
            throw new RuntimeException("권한이 없습니다.");
        }

        notification.setIsRead(true);
        notificationRepo.save(notification);
    }

    /**
     * 모든 알림 읽음 처리
     */
    @Transactional
    public void markAllAsRead(PrincipalDto principal) {
        notificationRepo.markAllAsRead(principal.getId());
    }

    /**
     * 사용자의 모든 알림 삭제
     */
    @Transactional
    public void deleteAllNotifications(PrincipalDto principal) {
        notificationRepo.deleteByUserId(principal.getId());
    }

    /**
     * Entity to DTO 변환
     */
    private NotificationResDto toDto(Notification notification) {
        return NotificationResDto.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .reservationId(notification.getReservationId())
                .createdAt(notification.getCreatedAt() != null ?
                        notification.getCreatedAt().toLocalDateTime() : null)
                .build();
    }
}

