package com.example.ragcsdn.service.impl;

import com.example.ragcsdn.entity.Article;
import com.example.ragcsdn.enums.ArticleStatus;
import com.example.ragcsdn.mapper.ArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文章状态写入器
 *
 * 使用独立事务（REQUIRES_NEW）更新文章失败状态，确保即使外层事务回滚，
 * 失败记录也能持久化。这解决了 @Transactional + catch-rethrow 场景下
 * 失败状态被一同回滚的问题。
 */
@Component
public class ArticleStatusWriter {

    @Autowired
    private ArticleMapper articleMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Article article, String reason) {
        article.setStatus(ArticleStatus.FAILED.getCode());
        article.setFailReason(reason);
        articleMapper.update(article);
    }
}

