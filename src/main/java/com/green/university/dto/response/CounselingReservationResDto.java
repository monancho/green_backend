package com.green.university.dto.response;

import com.green.university.enums.CounselingReservationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CounselingReservationResDto {
    private Long reservationId;
    private Long slotId;
    private Integer studentId;
    private String studentName;
    private CounselingReservationStatus status;
    private String studentMemo;
    private LocalDateTime createdAt;
    private LocalDateTime canceledAt;
}
