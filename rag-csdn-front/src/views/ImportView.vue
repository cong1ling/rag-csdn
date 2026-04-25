<template>
  <AppShell
    eyebrow="Article Import"
    title="把 CSDN 文章导入到知识库"
    subtitle="支持单篇导入、按作者批量导入公开文章，以及一键导入首页公开推荐内容。"
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
          <el-form-item label="CSDN 登录态 Cookie">
            <el-input
              v-model.trim="sessionForm.cookie"
              type="textarea"
              :rows="5"
              placeholder="粘贴你自己账号当前有效的 CSDN Cookie。支持直接粘贴完整 Cookie: ... 请求头。"
            />
          </el-form-item>

          <div class="credential-panel">
            <div class="credential-status">
              <strong>登录态状态</strong>
              <p>
                {{
                  authStore.user?.hasCsdnSession
                    ? `已保存，可用于导入你当前账号可访问的文章${authStore.user?.csdnSessionUpdateTime ? `（更新时间：${authStore.user.csdnSessionUpdateTime}）` : ""}`
                    : "未保存。未配置时仅适合抓取公开可访问文章。"
                }}
              </p>
            </div>
            <div class="toolbar">
              <el-button plain :loading="credentialSubmitting" @click="handleSaveSession">
                保存登录态
              </el-button>
              <el-button
                plain
                type="danger"
                :disabled="!authStore.user?.hasCsdnSession"
                :loading="credentialClearing"
                @click="handleClearSession"
              >
                清除登录态
              </el-button>
            </div>
          </div>

          <el-form-item label="CSDN 文章 URL">
            <el-input
              v-model.trim="form.articleUrl"
              placeholder="例如 https://blog.csdn.net/weixin_12345678/article/details/147000001"
            />
          </el-form-item>

          <div class="single-import-panel">
            <div class="single-import-copy">
              <strong>单篇导入</strong>
              <p class="card-caption">输入文章 URL 后可直接提交。导入完成后可在右侧查看当前请求反馈。</p>
            </div>
            <div class="toolbar single-import-toolbar">
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

          <div class="batch-grid">
            <div class="batch-card">
              <div class="eyebrow">Batch Import</div>
              <h3>按作者导入公开文章</h3>
              <p class="card-caption">输入作者主页 URL，系统会抓取当前公开可见的文章列表并批量提交导入。</p>
              <el-form-item label="作者主页 URL">
                <el-input
                  v-model.trim="authorForm.authorUrl"
                  placeholder="例如 https://blog.csdn.net/某个作者"
                />
              </el-form-item>
              <div class="inline-fields">
                <el-form-item label="最大文章数">
                  <el-input-number v-model="authorForm.maxArticles" :min="1" :max="100" />
                </el-form-item>
                <el-form-item label="最大页数">
                  <el-input-number v-model="authorForm.maxPages" :min="1" :max="20" />
                </el-form-item>
              </div>
              <el-button plain :loading="authorSubmitting" @click="handleAuthorImport">
                批量导入作者公开文章
              </el-button>
            </div>

            <div class="batch-card">
              <div class="eyebrow">Quick Start</div>
              <h3>一键导入首页公开推荐</h3>
              <p class="card-caption">从 CSDN 首页公开推荐流抓取文章链接，并按数量上限批量提交。</p>
              <el-form-item label="导入数量">
                <el-input-number v-model="recommendForm.limit" :min="1" :max="100" />
              </el-form-item>
              <el-button plain :loading="recommendSubmitting" @click="handleRecommendedImport">
                一键导入公开推荐
              </el-button>
            </div>
          </div>
        </el-form>

        <div class="surface-strong card-section action-section">
          <div class="action-header">
            <div class="eyebrow">使用说明</div>
            <p class="card-caption">
              1. 粘贴 CSDN 文章链接。2. 等待系统抓取正文并建立索引。3. 导入完成后，去文章列表查看结果并开始问答。
            </p>
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
              <p>系统会优先使用你已保存的 CSDN 登录态抓取文章；未保存时仅能稳定处理公开可访问内容。若正文为空、登录态失效或页面结构异常，导入可能失败。</p>
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

          <transition name="el-zoom-in-top">
            <div v-if="batchResult" class="result-card batch-result-card">
              <div class="result-header">
                <strong>批量导入结果</strong>
                <StatusPill
                  :label="batchResult.failedCount ? '部分完成' : '已提交'"
                  :tone="batchResult.failedCount ? 'warning' : 'success'"
                />
              </div>

              <div class="result-details">
                <div class="detail-item">
                  <span class="detail-label">导入模式</span>
                  <span class="detail-value title-value">{{ batchResult.mode }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">目标来源</span>
                  <a class="detail-value title-value" :href="batchResult.target" target="_blank" rel="noreferrer">
                    {{ batchResult.target }}
                  </a>
                </div>

                <div class="detail-stats stats-quad">
                  <div class="stat-box">
                    <span class="stat-value">{{ batchResult.discoveredCount }}</span>
                    <span class="stat-label">发现链接</span>
                  </div>
                  <div class="stat-box">
                    <span class="stat-value">{{ batchResult.submittedCount }}</span>
                    <span class="stat-label">已提交</span>
                  </div>
                  <div class="stat-box">
                    <span class="stat-value">{{ batchResult.duplicateCount }}</span>
                    <span class="stat-label">重复跳过</span>
                  </div>
                  <div class="stat-box">
                    <span class="stat-value">{{ batchResult.failedCount }}</span>
                    <span class="stat-label">失败</span>
                  </div>
                </div>

                <div v-if="batchResult.items?.length" class="batch-items">
                  <div v-for="item in batchResult.items.slice(0, 8)" :key="`${item.sourceId}-${item.status}`" class="batch-item">
                    <div class="batch-item-head">
                      <span class="badge-inline code-text">{{ item.sourceId }}</span>
                      <StatusPill
                        :label="item.status"
                        :tone="item.status === 'SUBMITTED' ? 'success' : item.status === 'SKIPPED_DUPLICATE' ? 'warning' : 'danger'"
                      />
                    </div>
                    <div class="batch-item-title">{{ item.title || item.sourceUrl }}</div>
                    <div class="muted">{{ item.message }}</div>
                  </div>
                </div>
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
import { useAuthStore } from "../stores/auth";
import { formatDateTime } from "../utils/format";
import { notifyError } from "../utils/error";

const authStore = useAuthStore();
const inlineError = ref("");
const submitting = ref(false);
const credentialSubmitting = ref(false);
const credentialClearing = ref(false);
const authorSubmitting = ref(false);
const recommendSubmitting = ref(false);
const requestState = ref("等待提交。");
const result = ref(null);
const batchResult = ref(null);
const form = reactive({
  articleUrl: "",
});
const sessionForm = reactive({
  cookie: "",
});
const authorForm = reactive({
  authorUrl: "",
  maxArticles: 20,
  maxPages: 5,
});
const recommendForm = reactive({
  limit: 12,
});

function clearForm() {
  form.articleUrl = "";
  authorForm.authorUrl = "";
  ElMessage.success("输入已清空");
}

async function handleSaveSession() {
  inlineError.value = "";
  if (!sessionForm.cookie) {
    inlineError.value = "请先粘贴你当前有效的 CSDN Cookie。";
    return;
  }

  credentialSubmitting.value = true;
  try {
    await authStore.saveCsdnSession({ cookie: sessionForm.cookie });
    sessionForm.cookie = "";
    ElMessage.success("CSDN 登录态已保存");
  } catch (error) {
    inlineError.value = notifyError(error).message;
  } finally {
    credentialSubmitting.value = false;
  }
}

async function handleClearSession() {
  inlineError.value = "";
  credentialClearing.value = true;
  try {
    await authStore.clearCsdnSession();
    sessionForm.cookie = "";
    ElMessage.success("CSDN 登录态已清除");
  } catch (error) {
    inlineError.value = notifyError(error).message;
  } finally {
    credentialClearing.value = false;
  }
}

async function handleSubmit() {
  inlineError.value = "";
  result.value = null;
  batchResult.value = null;

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

async function handleAuthorImport() {
  inlineError.value = "";
  result.value = null;
  batchResult.value = null;

  if (!authorForm.authorUrl) {
    inlineError.value = "请填写作者主页 URL。";
    return;
  }

  authorSubmitting.value = true;
  requestState.value = "正在抓取作者公开文章列表并批量提交导入...";
  try {
    batchResult.value = await articlesApi.importAuthorArticles(authorForm);
    requestState.value = "作者公开文章批量导入任务已提交。";
    ElMessage.success("作者公开文章已批量提交导入");
  } catch (error) {
    inlineError.value = notifyError(error).message;
    requestState.value = "作者公开文章批量导入失败。";
  } finally {
    authorSubmitting.value = false;
  }
}

async function handleRecommendedImport() {
  inlineError.value = "";
  result.value = null;
  batchResult.value = null;

  recommendSubmitting.value = true;
  requestState.value = "正在抓取首页公开推荐文章并批量提交导入...";
  try {
    batchResult.value = await articlesApi.importRecommendedArticles(recommendForm);
    requestState.value = "首页公开推荐文章导入任务已提交。";
    ElMessage.success("公开推荐文章已批量提交导入");
  } catch (error) {
    inlineError.value = notifyError(error).message;
    requestState.value = "公开推荐文章批量导入失败。";
  } finally {
    recommendSubmitting.value = false;
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

.credential-panel {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid var(--rb-border);
  border-radius: var(--rb-radius-md);
  background: var(--rb-panel-strong);
}

.credential-status {
  flex: 1;
}

.credential-status strong {
  color: var(--rb-text);
}

.credential-status p {
  margin-top: 8px;
  color: var(--rb-text-soft);
  font-size: 0.92rem;
  line-height: 1.6;
}

.batch-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.single-import-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid var(--rb-border);
  border-radius: var(--rb-radius-md);
  background: var(--rb-panel-strong);
}

.single-import-copy {
  flex: 1;
}

.single-import-copy strong {
  color: var(--rb-text);
}

.single-import-copy p {
  margin-top: 8px;
}

.single-import-toolbar {
  flex-wrap: wrap;
  justify-content: flex-end;
}

.batch-card {
  padding: 16px;
  border: 1px solid var(--rb-border);
  border-radius: var(--rb-radius-md);
  background: var(--rb-panel-strong);
}

.batch-card h3 {
  margin: 8px 0 10px;
  color: var(--rb-text);
}

.inline-fields {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.import-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--rb-text);
}

/* Action Section */
.action-section {
  margin-top: 1.25rem;
  padding-top: 1.25rem;
  border-top: 1px solid var(--rb-border);
  background: transparent;
}

.action-header {
  flex: 1;
}

.full-width {
  width: 100%;
}

/* Feedback Section */
.feedback-section {
  position: sticky;
  top: 24px;
  align-self: start;
  max-height: calc(100vh - 48px);
  overflow: hidden;
}

.stack {
  max-height: calc(100vh - 180px);
  overflow-y: auto;
  padding-right: 6px;
}

.stack::-webkit-scrollbar,
.batch-items::-webkit-scrollbar {
  width: 8px;
}

.stack::-webkit-scrollbar-thumb,
.batch-items::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.45);
  border-radius: 999px;
}

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

.stats-quad {
  grid-template-columns: repeat(2, 1fr);
}

.batch-result-card {
  margin-top: 16px;
}

.batch-items {
  display: grid;
  gap: 12px;
  margin-top: 16px;
  max-height: 340px;
  overflow-y: auto;
  padding-right: 6px;
}

.batch-item {
  padding: 12px;
  border-radius: var(--rb-radius-sm);
  border: 1px solid var(--rb-border);
  background: var(--rb-panel);
}

.batch-item-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.batch-item-title {
  color: var(--rb-text);
  font-weight: 600;
  line-height: 1.5;
  margin-bottom: 4px;
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
  .batch-grid,
  .inline-fields {
    grid-template-columns: 1fr;
  }

  .credential-panel,
  .single-import-panel,
  .action-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .feedback-section {
    position: static;
    max-height: none;
  }

  .stack,
  .batch-items {
    max-height: none;
    overflow: visible;
    padding-right: 0;
  }
}
</style>
