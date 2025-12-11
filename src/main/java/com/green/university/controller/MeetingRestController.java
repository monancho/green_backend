package com.green.university.controller;

import com.green.university.dto.ChatMessageDto;
import com.green.university.service.MeetingChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MeetingRestController {

    private final MeetingChatService meetingChatService;

    @GetMapping("/api/meetings/{meetingId}/chat/messages")
    public List<ChatMessageDto> getMessages(
            @PathVariable Integer meetingId,
            @RequestParam(required = false) Integer afterId,
            @RequestParam(required = false) Integer beforeId,
            @RequestParam(required = false) Integer size
    ) {
        if (afterId != null) {
            return meetingChatService.getMessagesAfter(meetingId, afterId, size);
        } else if (beforeId != null) {
            return meetingChatService.getMessagesBefore(meetingId, beforeId, size);
        } else {
            return meetingChatService.getRecentMessages(meetingId, size);
        }
    }
}
