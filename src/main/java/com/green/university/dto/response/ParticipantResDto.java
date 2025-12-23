package com.green.university.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantResDto {
    private Integer userId;
    private String name;        // displayName 우선, fallback 처리
    private String role;        // HOST | PARTICIPANT
    private String inviteStatus; // INVITED | JOINED (DB 기준, 참고용)
}
