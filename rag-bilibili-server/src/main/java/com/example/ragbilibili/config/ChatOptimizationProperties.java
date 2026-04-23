package com.example.ragbilibili.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag.chat")
public class ChatOptimizationProperties {
    private Integer maxHistory = 10;
    private Boolean queryRewriteEnabled = true;
    private Boolean queryUnderstandingEnabled = true;
    private Boolean hydeEnabled = true;
    private Boolean decompositionEnabled = true;
    private Integer maxDecomposedQueries = 4;
    private Boolean hybridSearchEnabled = true;
    private Integer keywordTopK = 8;
    private Boolean rerankEnabled = true;
    private Integer rerankCandidateTopK = 20;
    private Boolean modelRerankEnabled = false;
    private Integer modelRerankTopK = 8;
    private Boolean dynamicTopKEnabled = true;
    private Integer simpleTopK = 3;
    private Integer normalTopK = 5;
    private Integer complexTopK = 8;
    private Boolean summaryEnabled = true;
    private Integer summaryTriggerMessages = 10;
    private Integer summaryRecentMessages = 6;
    private Integer summaryMaxLength = 150;
    private Boolean confidenceAwareEnabled = true;

    public Integer getMaxHistory() {
        return maxHistory;
    }

    public void setMaxHistory(Integer maxHistory) {
        this.maxHistory = maxHistory;
    }

    public Boolean getQueryRewriteEnabled() {
        return queryRewriteEnabled;
    }

    public void setQueryRewriteEnabled(Boolean queryRewriteEnabled) {
        this.queryRewriteEnabled = queryRewriteEnabled;
    }

    public Boolean getQueryUnderstandingEnabled() {
        return queryUnderstandingEnabled;
    }

    public void setQueryUnderstandingEnabled(Boolean queryUnderstandingEnabled) {
        this.queryUnderstandingEnabled = queryUnderstandingEnabled;
    }

    public Boolean getHydeEnabled() {
        return hydeEnabled;
    }

    public void setHydeEnabled(Boolean hydeEnabled) {
        this.hydeEnabled = hydeEnabled;
    }

    public Boolean getDecompositionEnabled() {
        return decompositionEnabled;
    }

    public void setDecompositionEnabled(Boolean decompositionEnabled) {
        this.decompositionEnabled = decompositionEnabled;
    }

    public Integer getMaxDecomposedQueries() {
        return maxDecomposedQueries;
    }

    public void setMaxDecomposedQueries(Integer maxDecomposedQueries) {
        this.maxDecomposedQueries = maxDecomposedQueries;
    }

    public Boolean getHybridSearchEnabled() {
        return hybridSearchEnabled;
    }

    public void setHybridSearchEnabled(Boolean hybridSearchEnabled) {
        this.hybridSearchEnabled = hybridSearchEnabled;
    }

    public Integer getKeywordTopK() {
        return keywordTopK;
    }

    public void setKeywordTopK(Integer keywordTopK) {
        this.keywordTopK = keywordTopK;
    }

    public Boolean getRerankEnabled() {
        return rerankEnabled;
    }

    public void setRerankEnabled(Boolean rerankEnabled) {
        this.rerankEnabled = rerankEnabled;
    }

    public Integer getRerankCandidateTopK() {
        return rerankCandidateTopK;
    }

    public void setRerankCandidateTopK(Integer rerankCandidateTopK) {
        this.rerankCandidateTopK = rerankCandidateTopK;
    }

    public Boolean getModelRerankEnabled() {
        return modelRerankEnabled;
    }

    public void setModelRerankEnabled(Boolean modelRerankEnabled) {
        this.modelRerankEnabled = modelRerankEnabled;
    }

    public Integer getModelRerankTopK() {
        return modelRerankTopK;
    }

    public void setModelRerankTopK(Integer modelRerankTopK) {
        this.modelRerankTopK = modelRerankTopK;
    }

    public Boolean getDynamicTopKEnabled() {
        return dynamicTopKEnabled;
    }

    public void setDynamicTopKEnabled(Boolean dynamicTopKEnabled) {
        this.dynamicTopKEnabled = dynamicTopKEnabled;
    }

    public Integer getSimpleTopK() {
        return simpleTopK;
    }

    public void setSimpleTopK(Integer simpleTopK) {
        this.simpleTopK = simpleTopK;
    }

    public Integer getNormalTopK() {
        return normalTopK;
    }

    public void setNormalTopK(Integer normalTopK) {
        this.normalTopK = normalTopK;
    }

    public Integer getComplexTopK() {
        return complexTopK;
    }

    public void setComplexTopK(Integer complexTopK) {
        this.complexTopK = complexTopK;
    }

    public Boolean getSummaryEnabled() {
        return summaryEnabled;
    }

    public void setSummaryEnabled(Boolean summaryEnabled) {
        this.summaryEnabled = summaryEnabled;
    }

    public Integer getSummaryTriggerMessages() {
        return summaryTriggerMessages;
    }

    public void setSummaryTriggerMessages(Integer summaryTriggerMessages) {
        this.summaryTriggerMessages = summaryTriggerMessages;
    }

    public Integer getSummaryRecentMessages() {
        return summaryRecentMessages;
    }

    public void setSummaryRecentMessages(Integer summaryRecentMessages) {
        this.summaryRecentMessages = summaryRecentMessages;
    }

    public Integer getSummaryMaxLength() {
        return summaryMaxLength;
    }

    public void setSummaryMaxLength(Integer summaryMaxLength) {
        this.summaryMaxLength = summaryMaxLength;
    }

    public Boolean getConfidenceAwareEnabled() {
        return confidenceAwareEnabled;
    }

    public void setConfidenceAwareEnabled(Boolean confidenceAwareEnabled) {
        this.confidenceAwareEnabled = confidenceAwareEnabled;
    }
}
