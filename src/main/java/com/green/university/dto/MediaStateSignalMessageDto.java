package com.green.university.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaStateSignalMessageDto {

    private Integer meetingId;
    private Integer userId;
    private String display;

    private Boolean audio;         // true = 켬, false = 끔
    private Boolean video;         // true = 켬, false = 끔

    private Boolean videoDeviceLost;

    private String videoSource;        // "camera" | "screen"
    private Boolean screenSoftMuted;   // true면 screen 송출만 OFF (캡처는 유지)
    private Boolean screenCapturing;   // optional

    private Long ts;
    private String type = "MEDIA_STATE"; // 나중에 타입 늘릴 때 구분용
}
