package com.green.university.repository;

import com.green.university.repository.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 알림 JPA Repository
 */
public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {

    /**
     * 사용자 ID로 읽지 않은 알림 목록 조회 (최신순)
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Integer userId);

    /**
     * 사용자 ID로 모든 알림 목록 조회 (최신순)
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);

    /**
     * 사용자 ID로 읽지 않은 알림 개수 조회
     */
    long countByUserIdAndIsReadFalse(Integer userId);

    /**
     * 알림 읽음 처리
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id")
    void markAsRead(@Param("id") Long id);

    /**
     * 사용자의 모든 알림 읽음 처리
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId")
    void markAllAsRead(@Param("userId") Integer userId);

    /**
     * 사용자의 모든 알림 삭제
     */
    void deleteByUserId(Integer userId);

    /**
     * 특정 예약 ID와 타입으로 알림이 이미 존재하는지 확인
     */
    boolean existsByReservationIdAndType(Long reservationId, String type);
}

