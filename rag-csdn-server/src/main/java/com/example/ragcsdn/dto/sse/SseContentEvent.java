package com.example.ragcsdn.dto.sse;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSE Content事件
 */
@Data
@NoArgsConstructor
public class SseContentEvent {
    private String type = "content";
    private String delta;

    public SseContentEvent(String delta) {
        this.delta = delta;
    }
}

