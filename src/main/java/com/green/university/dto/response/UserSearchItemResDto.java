package com.green.university.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSearchItemResDto {
    private Integer userId;
    private String name;
    private String role;   // "student" | "professor" | "staff"
    private String email;  // 선택사항이지만 UI에 유용
    private boolean alreadyInMeeting;
}
