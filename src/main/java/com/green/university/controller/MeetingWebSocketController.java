package com.green.university.controller;

import com.green.university.dto.ChatMessageDto;
import com.green.university.service.MeetingChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MeetingWebSocketController {

    private final MeetingChatService meetingChatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/meetings/{meetingId}/chat")
    public void handleChat(
            @DestinationVariable Integer meetingId,
            ChatMessageDto payload
    ) {
        // 0) payload null 체크
        if (payload == null) {
            log.warn("[MeetingWebSocketController] payload 가 null 입니다.");
            return;
        }

        log.debug("[MeetingWebSocketController] 수신 payload={}", payload);

        // 1) 메시지 내용 검증
        String messageText = payload.getMessage();
        if (messageText == null || messageText.trim().isEmpty()) {
            log.debug("[MeetingWebSocketController] 빈 메시지 전송 시도. 무시. payload={}", payload);
            return;
        }

        // 2) 사용자 정보 (전부 payload 기준)
        Integer userId = payload.getUserId();
        String displayName = payload.getDisplayName();

        if (userId == null) {
            log.warn("[MeetingWebSocketController] userId 가 없습니다. payload={}", payload);
            return;
        }
        if (displayName == null || displayName.isBlank()) {
            displayName = "참가자";
        }

        // 3) 서비스 호출 (DB 저장)
        ChatMessageDto saved = meetingChatService.saveChatMessage(
                meetingId,
                userId,
                displayName,
                messageText
        );

        // 4) 구독자에게 브로드캐스트
        String destination = String.format("/sub/meetings/%d/chat", meetingId);
        messagingTemplate.convertAndSend(destination, saved);

        log.debug("[MeetingWebSocketController] 채팅 전송 완료. dest={}, saved={}", destination, saved);
    }
}
