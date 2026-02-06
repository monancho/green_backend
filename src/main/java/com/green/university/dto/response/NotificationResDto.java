package com.green.university.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResDto {
    private Long id;
    private Integer userId;
    private String type;
    private String message;
    private Boolean isRead;
    private Long reservationId;
    private LocalDateTime createdAt;
}


