package com.green.university.controller;

import com.green.university.dto.ChatMessageDto;
import com.green.university.dto.MediaStateSignalMessageDto;
import com.green.university.dto.MediaStateSnapshotMessageDto;
import com.green.university.presence.MediaStateStore;
import com.green.university.service.MeetingChatService;
import com.green.university.service.MeetingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MeetingWebSocketController {

    private final MeetingChatService meetingChatService;
    private final SimpMessagingTemplate messagingTemplate;
    private  final MeetingService meetingService;

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor accessor =
                SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        accessor.setSessionId(sessionId);
        accessor.setLeaveMutable(true);
        return accessor.getMessageHeaders();
    }
    private final MediaStateStore mediaStateStore;
    @MessageMapping("/meetings/{meetingId}/chat")
    public void handleChat(
            @DestinationVariable Integer meetingId,
            @Payload ChatMessageDto payload
    ) {
        if (payload == null) {
            log.warn("[MeetingWebSocketController] payload 가 null 입니다.");
            return;
        }
        log.debug("[MeetingWebSocketController] 수신 payload={}", payload);

        String messageText = payload.getMessage();
        if (messageText == null || messageText.trim().isEmpty()) {
            log.debug("[MeetingWebSocketController] 빈 메시지 전송 시도. 무시. payload={}", payload);
            return;
        }

        Integer userId = payload.getUserId();
        String displayName = payload.getDisplayName();

        if (userId == null) {
            log.warn("[MeetingWebSocketController] userId 가 없습니다. payload={}", payload);
            return;
        }
        if (displayName == null || displayName.isBlank()) {
            displayName = "참가자";
        }

        ChatMessageDto saved = meetingChatService.saveChatMessage(
                meetingId,
                userId,
                displayName,
                messageText,
                payload.getType()
        );

        String destination = String.format("/sub/meetings/%d/chat", meetingId);
        messagingTemplate.convertAndSend(destination, saved);
        log.debug("[MeetingWebSocketController] 채팅 전송 완료. dest={}, saved={}", destination, saved);
    }

    @MessageMapping("/meetings/{meetingId}/signals")
    public void handleMediaSignal(
            @DestinationVariable Integer meetingId,
            @Payload MediaStateSignalMessageDto payload
    ) {
        if (payload == null) return;

        payload.setMeetingId(meetingId);

        if (payload.getType() == null || payload.getType().isBlank()) {
            payload.setType("MEDIA_STATE");
        }

        if (payload.getTs() == null) {
            payload.setTs(System.currentTimeMillis());
        }

        Integer userId = payload.getUserId();
        if (userId == null) return;

        if (payload.getDisplay() == null || payload.getDisplay().isBlank()) return;

        mediaStateStore.update(
                meetingId,
                userId,
                payload.getAudio(),
                payload.getVideo(),
                payload.getVideoDeviceLost(),
                payload.getDisplay(),
                payload.getVideoSource(),
                payload.getScreenSoftMuted(),
                payload.getScreenCapturing(),
                payload.getTs()
        );

        String destination = String.format("/sub/meetings/%d/signals", meetingId);
        messagingTemplate.convertAndSend(destination, payload);
    }


    @MessageMapping("/meetings/{meetingId}/signals/snapshot")
    public void handleMediaSnapshotRequest(
            @DestinationVariable Integer meetingId,
            @Header("simpSessionId") String sessionId
    ) {
        MediaStateSnapshotMessageDto snap = new MediaStateSnapshotMessageDto();
        snap.setMeetingId(meetingId);
        snap.setTs(System.currentTimeMillis());
        snap.setStates(mediaStateStore.list(meetingId));

        messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/signals",
                snap,
                createHeaders(sessionId)
        );
    }



}
