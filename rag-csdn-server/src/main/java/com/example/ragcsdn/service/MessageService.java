package com.example.ragcsdn.service;

import com.example.ragcsdn.dto.response.MessageResponse;
import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService {
    /**
     * 获取会话消息列表
     */
    List<MessageResponse> listMessages(Long sessionId, Long userId);
}

