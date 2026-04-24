package com.example.ragcsdn.dto.sse;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSE End事件
 */
@Data
@NoArgsConstructor
public class SseEndEvent {
    private String type = "end";
    private Long assistantMessageId;
    private String fullContent;
    private String queryIntent;
    private String rewrittenQuery;
    private String confidenceLabel;
    private Double confidenceScore;
    private Integer sourceCount;
    private Boolean knowledgeGap;
    private Boolean summaryUsed;

    public SseEndEvent(Long assistantMessageId, String fullContent) {
        this.assistantMessageId = assistantMessageId;
        this.fullContent = fullContent;
    }
}

