package com.example.ragcsdn.dto.sse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SseEventSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSerializeStartEventWithStableTypeField() throws Exception {
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(new SseStartEvent(11L)));

        assertEquals("start", json.get("type").asText());
        assertEquals(11L, json.get("userMessageId").asLong());
    }

    @Test
    void shouldSerializeContentEventWithDelta() throws Exception {
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(new SseContentEvent("增量内容")));

        assertEquals("content", json.get("type").asText());
        assertEquals("增量内容", json.get("delta").asText());
    }

    @Test
    void shouldSerializeEndEventWithAssistantMessageIdAndFullContent() throws Exception {
        SseEndEvent event = new SseEndEvent(22L, "完整回答");
        event.setQueryIntent("AMBIGUOUS");
        event.setRewrittenQuery("Spring Boot 默认端口是什么");
        event.setConfidenceLabel("LOW");
        event.setConfidenceScore(0.34d);
        event.setSourceCount(2);
        event.setKnowledgeGap(true);
        event.setSummaryUsed(true);

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(event));

        assertEquals("end", json.get("type").asText());
        assertEquals(22L, json.get("assistantMessageId").asLong());
        assertEquals("完整回答", json.get("fullContent").asText());
        assertEquals("AMBIGUOUS", json.get("queryIntent").asText());
        assertEquals("LOW", json.get("confidenceLabel").asText());
        assertEquals(2, json.get("sourceCount").asInt());
        assertEquals(true, json.get("knowledgeGap").asBoolean());
    }

    @Test
    void shouldSerializeErrorEventWithMessage() throws Exception {
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(new SseErrorEvent("请求失败")));

        assertEquals("error", json.get("type").asText());
        assertEquals("请求失败", json.get("message").asText());
    }
}

