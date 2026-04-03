<template>
  <AppShell
    eyebrow="Video Import"
    title="把单个 B 站视频导入到知识库"
    subtitle="填写视频标识和必要凭证后，即可把视频内容整理成可检索、可提问的知识资料。"
  >

    <div class="page-grid import-page">
      <section class="surface span-7 card-section">
        <div class="card-header">
          <div>
            <div class="eyebrow">Start Here</div>
            <h2>导入请求</h2>
            <p class="card-caption">支持输入 BV 号，或包含 BV 的完整 Bilibili 视频 URL。</p>
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
          <el-form-item label="BV 号或视频 URL">
            <el-input
              v-model.trim="form.bvidOrUrl"
              placeholder="例如 BV1DCfsBKExV 或 https://www.bilibili.com/video/BV1DCfsBKExV"
            />
          </el-form-item>

        <div class="credentials-group">
          <div class="group-header">
            <h3>身份凭证</h3>
            <el-button link type="primary" size="small" @click="guideVisible = true">
              如何获取？
            </el-button>
          </div>

          <el-form-item label="SESSDATA">
            <el-input v-model.trim="form.sessdata" type="password" show-password placeholder="Cookie: SESSDATA" />
          </el-form-item>
          <el-form-item label="bili_jct">
            <el-input v-model.trim="form.biliJct" type="password" show-password placeholder="Cookie: bili_jct" />
          </el-form-item>
          <el-form-item label="buvid3">
            <el-input v-model.trim="form.buvid3" type="password" show-password placeholder="Cookie: buvid3" />
          </el-form-item>
        </div>

    <el-dialog v-model="guideVisible" title="获取 B 站凭证指南" width="560px">
      <div class="guide-content">
        <p>导入视频需要 B 站的身份凭证（Cookies），请按照以下步骤操作：</p>
        <ol class="guide-steps">
          <li>在浏览器中登录 <a href="https://www.bilibili.com" target="_blank">Bilibili 官网</a>。</li>
          <li>按 <strong>F12</strong> (或鼠标右键“检查”) 打开开发者工具。</li>
          <li>切换到 <strong>Application</strong> (应用程序) 标签页。</li>
          <li>在左侧列表中找到 <strong>Cookies</strong>，并点击 <code>https://www.bilibili.com</code>。</li>
          <li>在右侧搜索框中分别搜索并复制以下字段的值：
            <ul>
              <li><code>SESSDATA</code></li>
              <li><code>bili_jct</code></li>
              <li><code>buvid3</code></li>
            </ul>
          </li>
        </ol>
        <el-alert type="info" :closable="false" show-icon style="margin-top: 20px;">
          这些凭证只会临时保留在本地，用于解析视频字幕。请勿将这些值泄露给他人。
        </el-alert>
      </div>
      <template #footer>
        <el-button type="primary" @click="guideVisible = false">我知道了</el-button>
      </template>
    </el-dialog>
        </el-form>

        <div class="surface-strong card-section action-section">
          <div class="action-header">
            <div class="eyebrow">使用说明</div>
            <p class="card-caption">
              1. 先填写视频 BV 号或视频链接。2. 再补充导入所需凭证。3. 导入完成后，去视频列表查看结果并开始问答。
            </p>
          </div>
          <div class="toolbar">
            <el-button type="primary" :loading="submitting" @click="handleSubmit">
              开始导入
              <el-icon class="el-icon--right"><VideoPlay /></el-icon>
            </el-button>
            <el-button @click="clearCredentials">
              清除凭证
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
              <strong>安全提示</strong>
              <p>为了保护账号信息，凭证只会临时保留在当前浏览器标签页中，方便你短时间内重复导入。关闭页面后将自动清除。</p>
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
                  <span class="detail-label">视频标识</span>
                  <span class="badge-inline code-text">{{ result.bvid }}</span>
                </div>

                <div class="detail-item">
                  <span class="detail-label">视频标题</span>
                  <span class="detail-value title-value">{{ result.title }}</span>
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
                <RouterLink to="/videos" class="full-width">
                  <el-button type="success" plain class="full-width">
                    去查看视频列表
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
  VideoPlay, Delete, QuestionFilled, InfoFilled,
  Loading, Warning, ArrowRight
} from "@element-plus/icons-vue";
import { reactive, ref, watch } from "vue";
import { RouterLink } from "vue-router";

import AppShell from "../components/AppShell.vue";
import StatusPill from "../components/StatusPill.vue";
import { videosApi } from "../api/videos";
import { formatDateTime } from "../utils/format";
import { notifyError } from "../utils/error";

const STORAGE_KEY = "rag-bilibili-credentials";

const inlineError = ref("");
const submitting = ref(false);
const guideVisible = ref(false);
const requestState = ref("等待提交。");
const result = ref(null);
const form = reactive({
  bvidOrUrl: "",
  sessdata: "",
  biliJct: "",
  buvid3: "",
});

loadCredentials();

watch(
  () => ({ ...form }),
  () => {
    saveCredentials();
  },
  { deep: true }
);

function saveCredentials() {
  const payload = {
    sessdata: form.sessdata,
    biliJct: form.biliJct,
    buvid3: form.buvid3,
  };
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(payload));
}

function loadCredentials() {
  const raw = sessionStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return;
  }
  try {
    const payload = JSON.parse(raw);
    form.sessdata = payload.sessdata || "";
    form.biliJct = payload.biliJct || "";
    form.buvid3 = payload.buvid3 || "";
  } catch {
    sessionStorage.removeItem(STORAGE_KEY);
  }
}

function clearCredentials() {
  form.sessdata = "";
  form.biliJct = "";
  form.buvid3 = "";
  sessionStorage.removeItem(STORAGE_KEY);
  ElMessage.success("凭证已清除");
}

async function handleSubmit() {
  inlineError.value = "";
  result.value = null;

  if (!form.bvidOrUrl || !form.sessdata || !form.biliJct || !form.buvid3) {
    inlineError.value = "请完整填写 BV/URL 与 3 个凭证字段。";
    return;
  }

  submitting.value = true;
  requestState.value = "正在整理视频内容，请稍候...";

  try {
    result.value = await videosApi.importVideo(form);
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

.credentials-group {
  margin-top: 2rem;
  padding: 1.5rem;
  background: var(--rb-panel-strong);
  border-radius: var(--rb-radius-md);
  border: 1px solid var(--rb-border);
}

.group-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.group-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: var(--rb-text);
}

.help-icon {
  color: var(--rb-text-muted);
  cursor: help;
  font-size: 1.1rem;
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
