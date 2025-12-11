package com.green.university.dto;

import com.green.university.enums.CounselingSlotStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CounselingSlotResDto {
    private Long slotId;
    private Integer professorId;
    private String professorName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private CounselingSlotStatus status;
    private Integer meetingId;
}
