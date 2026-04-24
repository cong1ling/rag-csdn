package com.example.ragcsdn.dto.sse;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSE Start事件
 */
@Data
@NoArgsConstructor
public class SseStartEvent {
    private String type = "start";
    private Long userMessageId;

    public SseStartEvent(Long userMessageId) {
        this.userMessageId = userMessageId;
    }
}

