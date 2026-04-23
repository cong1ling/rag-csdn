package com.example.ragbilibili.dto.sse;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSE Error事件
 */
@Data
@NoArgsConstructor
public class SseErrorEvent {
    private String type = "error";
    private String message;

    public SseErrorEvent(String message) {
        this.message = message;
    }
}
