package com.green.university.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateSingleSlotReqDto {
    private LocalDateTime startAt;  // 2025-12-08T10:00
    private LocalDateTime endAt;    // 2025-12-08T11:00
}
