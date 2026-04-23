package com.example.ragbilibili.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 配置
 */
@Configuration
@EnableConfigurationProperties({ChatOptimizationProperties.class, ChunkingProperties.class})
public class SpringAIConfig {
    /**
     * 配置 ChatClient.Builder
     */
    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }
}
