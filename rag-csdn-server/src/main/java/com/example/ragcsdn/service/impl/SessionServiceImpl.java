package com.example.ragcsdn.service.impl;

import com.example.ragcsdn.dto.request.CreateSessionRequest;
import com.example.ragcsdn.dto.response.SessionResponse;
import com.example.ragcsdn.entity.Session;
import com.example.ragcsdn.entity.Article;
import com.example.ragcsdn.enums.SessionType;
import com.example.ragcsdn.exception.BusinessException;
import com.example.ragcsdn.exception.ErrorCode;
import com.example.ragcsdn.mapper.MessageMapper;
import com.example.ragcsdn.mapper.SessionMapper;
import com.example.ragcsdn.mapper.ArticleMapper;
import com.example.ragcsdn.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionServiceImpl implements SessionService {
    @Autowired
    private SessionMapper sessionMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private MessageMapper messageMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public SessionResponse createSession(CreateSessionRequest request, Long userId) {
        // 验证会话类型
        String normalizedSessionType = SessionType.normalize(request.getSessionType());
        if (!SessionType.isValid(request.getSessionType())) {
            throw new BusinessException(ErrorCode.SESSION_TYPE_ERROR);
        }

        // 如果是单文章对话，验证文章ID
        if (SessionType.isSingleArticle(normalizedSessionType)) {
            if (request.getArticleId() == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR);
            }
            Article article = articleMapper.selectById(request.getArticleId());
            if (article == null || !article.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.VIDEO_NOT_FOUND);
            }
        }

        // 创建会话
        Session session = new Session();
        session.setUserId(userId);
        session.setSessionType(normalizedSessionType);
        session.setArticleId(request.getArticleId());
        session.setConversationSummary(null);
        session.setSummaryUpdateTime(null);
        session.setCreateTime(LocalDateTime.now());

        sessionMapper.insert(session);

        return convertToResponse(session);
    }

    @Override
    public List<SessionResponse> listSessions(Long userId) {
        List<Session> sessions = sessionMapper.selectByUserId(userId);
        return sessions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SessionResponse getSession(Long sessionId, Long userId) {
        Session session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }
        return convertToResponse(session);
    }

    @Override
    @Transactional
    public void deleteSession(Long sessionId, Long userId) {
        // 验证会话是否存在且属于当前用户
        Session session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }

        // 删除会话关联的消息
        messageMapper.deleteBySessionId(sessionId);

        // 删除会话
        sessionMapper.deleteById(sessionId);
    }

    private SessionResponse convertToResponse(Session session) {
        SessionResponse response = new SessionResponse();
        response.setId(session.getId());
        response.setSessionType(SessionType.normalize(session.getSessionType()));
        response.setArticleId(session.getArticleId());
        response.setCreateTime(session.getCreateTime().format(FORMATTER));
        response.setConversationSummary(session.getConversationSummary());
        if (session.getSummaryUpdateTime() != null) {
            response.setSummaryUpdateTime(session.getSummaryUpdateTime().format(FORMATTER));
        }

        // 如果是单文章对话，查询文章标题
        if (session.getArticleId() != null) {
            Article article = articleMapper.selectById(session.getArticleId());
            if (article != null) {
                response.setArticleTitle(article.getTitle());
            }
        }

        return response;
    }
}

