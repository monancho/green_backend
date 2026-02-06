package com.green.university.dto;

import com.green.university.presence.MediaStateStore;
import lombok.Data;

import java.util.List;

@Data
public class MediaStateSnapshotMessageDto {
    private Integer meetingId;
    private String type = "MEDIA_SNAPSHOT";
    private Long ts;
    private List<MediaStateStore.State> states;
}