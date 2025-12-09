package com.green.university.dto;

import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class CreateWeeklyPatternReqDto {

    private LocalDate weekStartDate;  // 기준 주 월요일
    private LocalDate repeatEndDate;  // 반복 종료일

    private List<WeeklyPatternItem> items;

    @Data
    public static class WeeklyPatternItem {
        private DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
