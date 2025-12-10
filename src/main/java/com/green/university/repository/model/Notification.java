package com.green.university.repository.model;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

/**
 * 알림 정보를 나타내는 엔티티
 */
@Data
@Entity
@Table(name = "notification_tb")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 알림을 받는 사용자 ID (교수 또는 학생)
     */
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    /**
     * 알림 타입: RESERVATION_REQUEST (예약 신청), RESERVATION_APPROVED (예약 수락)
     */
    @Column(name = "type", nullable = false, length = 50)
    private String type;

    /**
     * 알림 메시지
     */
    @Column(name = "message", nullable = false, length = 500)
    private String message;

    /**
     * 읽음 여부
     */
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    /**
     * 관련 예약 ID (선택적)
     */
    @Column(name = "reservation_id")
    private Long reservationId;

    /**
     * 생성 시간
     */
    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;
}

