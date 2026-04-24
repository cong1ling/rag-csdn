<template>
  <AppShell
    eyebrow="Article Import"
    title="把单篇 CSDN 文章导入到知识库"
    subtitle="填写 CSDN 社区文章链接后，系统会自动抓取正文、切分并写入向量知识库。"
  >

    <div class="page-grid import-page">
      <section class="surface span-7 card-section">
        <div class="card-header">
          <div>
            <div class="eyebrow">Start Here</div>
            <h2>导入请求</h2>
            <p class="card-caption">支持输入完整的 CSDN 文章 URL，例如 `https://blog.csdn.net/.../article/details/...`。</p>
          </div>
          <StatusPill label="准备导入" tone="info" />
        </div>

        <el-alert
          v-if="inlineError"
          class="alert-inline"
          type="error"
          :title="inlineError"
          show-icon
          role="alert"
        />

        <el-form label-position="top" class="import-form">
          <el-form-item label="CSDN 文章 URL">
            <el-input
              v-model.trim="form.articleUrl"
              placeholder="例如 https://blog.csdn.net/weixin_12345678/article/details/147000001"
            />
          </el-form-item>
        </el-form>

        <div class="surface-strong card-section action-section">
          <div class="action-header">
            <div class="eyebrow">使用说明</div>
            <p class="card-caption">
              1. 粘贴 CSDN 文章链接。2. 等待系统抓取正文并建立索引。3. 导入完成后，去文章列表查看结果并开始问答。
            </p>
          </div>
          <div class="toolbar">
            <el-button type="primary" :loading="submitting" @click="handleSubmit">
              开始导入
              <el-icon class="el-icon--right"><VideoPlay /></el-icon>
            </el-button>
            <el-button @click="clearForm">
              清空输入
              <el-icon class="el-icon--right"><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </section>

      <section class="span-5 feedback-section surface card-section">
        <div class="card-header">
          <div>
            <div class="eyebrow">Feedback</div>
            <h2>执行反馈</h2>
          </div>
          <StatusPill v-if="submitting" label="处理中" tone="info" />
        </div>

        <div class="stack">
          <div class="status-card" :class="{ 'active': submitting }">
            <div class="status-icon">
              <el-icon v-if="submitting" class="is-loading"><Loading /></el-icon>
              <el-icon v-else><InfoFilled /></el-icon>
            </div>
            <div class="status-content">
              <strong>请求状态</strong>
              <p class="status-text">{{ requestState }}</p>
            </div>
          </div>

          <div class="info-card">
            <el-icon class="info-icon"><Warning /></el-icon>
            <div class="info-content">
              <strong>导入提示</strong>
              <p>系统会直接抓取公开可访问的 CSDN 文章正文。若文章设置了访问限制、正文为空或页面结构异常，导入可能失败。</p>
            </div>
          </div>

          <transition name="el-zoom-in-top">
            <div v-if="result" class="result-card">
              <div class="result-header">
                <strong>导入结果</strong>
                <StatusPill :label="result.status || 'SUCCESS'" tone="success" />
              </div>

              <div class="result-details">
                <div class="detail-item">
                  <span class="detail-label">文章标识</span>
                  <span class="badge-inline code-text">{{ result.sourceId || result.bvid }}</span>
                </div>

                <div class="detail-item">
                  <span class="detail-label">文章标题</span>
                  <span class="detail-value title-value">{{ result.title }}</span>
                </div>

                <div v-if="result.sourceUrl" class="detail-item">
                  <span class="detail-label">来源链接</span>
                  <a class="detail-value title-value" :href="result.sourceUrl" target="_blank" rel="noreferrer">
                    {{ result.sourceUrl }}
                  </a>
                </div>

                <div class="detail-stats">
                  <div class="stat-box">
                    <span class="stat-value">{{ result.chunkCount }}</span>
                    <span class="stat-label">内容片段</span>
                  </div>
                  <div class="stat-box">
                    <span class="stat-value text-sm">{{ formatDateTime(result.importTime).split(' ')[0] }}</span>
                    <span class="stat-label">导入时间</span>
                  </div>
                </div>
              </div>

              <div class="result-action">
                <RouterLink to="/articles" class="full-width">
                  <el-button type="success" plain class="full-width">
                    去查看文章列表
                    <el-icon class="el-icon--right"><ArrowRight /></el-icon>
                  </el-button>
                </RouterLink>
              </div>
            </div>
          </transition>
        </div>
      </section>
    </div>
  </AppShell>
</template>

<script setup>
import { ElMessage } from "element-plus";
import {
  VideoPlay, Delete, InfoFilled,
  Loading, Warning, ArrowRight
} from "@element-plus/icons-vue";
import { reactive, ref } from "vue";
import { RouterLink } from "vue-router";

import AppShell from "../components/AppShell.vue";
import StatusPill from "../components/StatusPill.vue";
import { articlesApi } from "../api/articles";
import { formatDateTime } from "../utils/format";
import { notifyError } from "../utils/error";

const inlineError = ref("");
const submitting = ref(false);
const requestState = ref("等待提交。");
const result = ref(null);
const form = reactive({
  articleUrl: "",
});

function clearForm() {
  form.articleUrl = "";
  ElMessage.success("输入已清空");
}

async function handleSubmit() {
  inlineError.value = "";
  result.value = null;

  if (!form.articleUrl) {
    inlineError.value = "请填写完整的 CSDN 文章链接。";
    return;
  }

  submitting.value = true;
  requestState.value = "正在抓取文章内容并建立索引，请稍候...";

  try {
    result.value = await articlesApi.importArticle(form);
    requestState.value = "导入完成，返回结果已写入当前页面。";
    ElMessage.success("导入成功");
  } catch (error) {
    inlineError.value = notifyError(error).message;
    requestState.value = "导入失败，请检查填写内容后重试。";
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.import-page {
  position: relative;
}

/* Form Styling */
.import-form {
  margin-top: 1.5rem;
}

.import-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--rb-text);
}

/* Action Section */
.action-section {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid var(--rb-border);
  background: transparent;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 2rem;
}

.action-header {
  flex: 1;
}

.full-width {
  width: 100%;
}

/* Feedback Section */
.status-card {
  display: flex;
  gap: 1rem;
  padding: 1.25rem;
  background: var(--rb-panel-strong);
  border-radius: var(--rb-radius-md);
  border: 1px solid var(--rb-border);
  margin-bottom: 1rem;
  transition: border-color 0.3s ease;
}

.status-card.active {
  border-color: #3b82f6;
  background: rgba(59, 130, 246, 0.05);
}

.status-icon {
  font-size: 1.5rem;
  color: var(--rb-text-muted);
  display: flex;
  align-items: flex-start;
}

.status-card.active .status-icon {
  color: #3b82f6;
}

.status-content {
  flex: 1;
}

.status-content strong {
  color: var(--rb-text);
}

.status-text {
  margin-top: 0.5rem;
  color: var(--rb-text-muted);
  font-size: 0.95rem;
  line-height: 1.5;
}

.info-card {
  display: flex;
  gap: 1rem;
  padding: 1.25rem;
  background: rgba(245, 158, 11, 0.05);
  border-radius: var(--rb-radius-md);
  border: 1px dashed rgba(245, 158, 11, 0.4);
  margin-bottom: 1.5rem;
}

.info-icon {
  font-size: 1.5rem;
  color: #f59e0b;
}

.info-content strong {
  color: #f59e0b;
}

.info-content p {
  margin-top: 0.5rem;
  color: var(--rb-text-muted);
  font-size: 0.9rem;
  line-height: 1.5;
}

/* Result Card */
.result-card {
  background: var(--rb-panel-strong);
  border: 1px solid rgba(16, 185, 129, 0.3);
  border-radius: var(--rb-radius-md);
  overflow: hidden;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid rgba(16, 185, 129, 0.2);
  background: rgba(16, 185, 129, 0.05);
}

.result-header strong {
  color: var(--rb-text);
}

.result-details {
  padding: 1.25rem;
}

.detail-item {
  margin-bottom: 1rem;
}

.detail-label {
  display: block;
  font-size: 0.85rem;
  color: var(--rb-text-muted);
  margin-bottom: 0.25rem;
}

.title-value {
  font-weight: 600;
  line-height: 1.4;
  display: block;
  color: var(--rb-text);
}

.detail-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-top: 1.5rem;
}

.stat-box {
  background: var(--rb-panel);
  padding: 1rem;
  border-radius: var(--rb-radius-sm);
  text-align: center;
  border: 1px solid var(--rb-border);
}

.stat-value {
  display: block;
  font-size: 1.5rem;
  font-weight: 700;
  color: #10b981;
  margin-bottom: 0.25rem;
}

.stat-value.text-sm {
  font-size: 1.1rem;
  line-height: 1.5rem;
}

.stat-label {
  font-size: 0.8rem;
  color: var(--rb-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.result-action {
  padding: 1.25rem;
  border-top: 1px solid var(--rb-border);
}

/* Guide Dialog */
.guide-content p {
  margin-bottom: 1.5rem;
  font-weight: 500;
  color: var(--rb-text);
}

.guide-steps {
  margin: 0;
  padding-left: 1.5rem;
  color: var(--rb-text-soft);
  line-height: 2;
}

.guide-steps code {
  background: var(--rb-bg-strong);
  padding: 2px 6px;
  border-radius: 4px;
  color: var(--rb-accent);
}

@media (max-width: 1024px) {
  .action-section {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
