package com.green.university.controller;

import com.green.university.dto.response.NotificationResDto;
import com.green.university.dto.response.PrincipalDto;
import com.green.university.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 알림 API 컨트롤러
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 읽지 않은 알림 목록 조회
     * GET /api/notifications/unread
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResDto>> getUnreadNotifications(
            @AuthenticationPrincipal PrincipalDto principal
    ) {
        List<NotificationResDto> notifications = notificationService.getUnreadNotifications(principal);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 모든 알림 목록 조회
     * GET /api/notifications
     */
    @GetMapping
    public ResponseEntity<List<NotificationResDto>> getAllNotifications(
            @AuthenticationPrincipal PrincipalDto principal
    ) {
        List<NotificationResDto> notifications = notificationService.getAllNotifications(principal);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 읽지 않은 알림 개수 조회
     * GET /api/notifications/unread/count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(
            @AuthenticationPrincipal PrincipalDto principal
    ) {
        long count = notificationService.getUnreadCount(principal);
        return ResponseEntity.ok(count);
    }

    /**
     * 알림 읽음 처리
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalDto principal
    ) {
        notificationService.markAsRead(id, principal);
        return ResponseEntity.ok().build();
    }

    /**
     * 모든 알림 읽음 처리
     * PUT /api/notifications/read-all
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal PrincipalDto principal
    ) {
        notificationService.markAllAsRead(principal);
        return ResponseEntity.ok().build();
    }

    /**
     * 모든 알림 삭제 (비우기)
     * DELETE /api/notifications
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllNotifications(
            @AuthenticationPrincipal PrincipalDto principal
    ) {
        notificationService.deleteAllNotifications(principal);
        return ResponseEntity.ok().build();
    }
}

