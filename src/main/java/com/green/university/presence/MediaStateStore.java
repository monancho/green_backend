// main/java/com/green/university/presence/MediaStateStore.java
package com.green.university.presence;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 미디어 상태(audio, video, videoDeviceLost)를 기억하는 메모리 저장소.
 */
@Component
public class MediaStateStore {

    @Data
    @Builder
    public static class State {
        private Integer meetingId;
        private Integer userId;
        private Boolean audio;
        private Boolean video;
        private Boolean videoDeviceLost;
        private String display;

        private String videoSource;
        private Boolean screenSoftMuted;
        private Boolean screenCapturing;

        private Long ts;
    }

    private final Map<Integer, Map<Integer, State>> byMeeting = new ConcurrentHashMap<>();

    public void update(Integer meetingId,
                       Integer userId,
                       Boolean audio,
                       Boolean video,
                       Boolean videoDeviceLost,
                       String display,
                       String videoSource,
                       Boolean screenSoftMuted,
                       Boolean screenCapturing,
                       Long ts) {

        if (meetingId == null || userId == null) return;

        Map<Integer, State> meetingMap =
                byMeeting.computeIfAbsent(meetingId, k -> new ConcurrentHashMap<>());

        State prev = meetingMap.get(userId);

        // ✅ out-of-order 방지 (prev 없으면 비교 불가)
        if (prev != null && ts != null && prev.getTs() != null && ts < prev.getTs()) {
            return;
        }

        if (prev == null) {
            meetingMap.put(userId, State.builder()
                    .meetingId(meetingId)
                    .userId(userId)
                    .audio(audio)
                    .video(video)
                    .videoDeviceLost(videoDeviceLost)
                    .display(display)
                    .videoSource(videoSource)
                    .screenSoftMuted(screenSoftMuted)
                    .screenCapturing(screenCapturing)
                    .ts(ts)
                    .build());
            return;
        }

        // ✅ 부분 업데이트(모르면 null 유지)
        if (audio != null) prev.setAudio(audio);
        if (video != null) prev.setVideo(video);
        if (videoDeviceLost != null) prev.setVideoDeviceLost(videoDeviceLost);
        if (display != null && !display.isBlank()) prev.setDisplay(display);

        if (videoSource != null) prev.setVideoSource(videoSource);
        if (screenSoftMuted != null) prev.setScreenSoftMuted(screenSoftMuted);
        if (screenCapturing != null) prev.setScreenCapturing(screenCapturing);

        if (ts != null) prev.setTs(ts);
    }
    public List<State> list(Integer meetingId) {
        Map<Integer, State> m = byMeeting.get(meetingId);
        if (m == null) return List.of();
        return new ArrayList<>(m.values());
    }

    public void remove(Integer meetingId, Integer userId) {
        Map<Integer, State> m = byMeeting.get(meetingId);
        if (m == null) return;
        m.remove(userId);
        if (m.isEmpty()) byMeeting.remove(meetingId);
    }

}
