package com.alibaba.cloud.ai.reader.csdn;

import com.example.ragcsdn.util.CsdnArticleUrlParser;
import org.springframework.util.Assert;

public class CsdnResource {

    private final String articleId;
    private final String articleUrl;

    public CsdnResource(String articleUrl) {
        Assert.hasText(articleUrl, "Article URL must not be empty");
        this.articleId = CsdnArticleUrlParser.parseId(articleUrl);
        this.articleUrl = CsdnArticleUrlParser.normalizeUrl(articleUrl);
    }

    public String getArticleId() {
        return articleId;
    }

    public String getArticleUrl() {
        return articleUrl;
    }
}

