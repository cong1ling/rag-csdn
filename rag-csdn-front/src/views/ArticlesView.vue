<template>
  <AppShell
    eyebrow="Article Library"
    title="已导入文章列表"
    subtitle="在这里管理已经整理好的 CSDN 文章资料。你可以查看内容概况、删除旧资料，或者直接围绕某篇文章开始提问。"
  >
    <template #header-actions>
        <div class="toolbar">
          <el-input v-model.trim="keyword" placeholder="搜索标题或文章标识" clearable class="toolbar-search">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button @click="loadVideos" :loading="loading">刷新列表</el-button>
      </div>
    </template>

    <div class="page-grid">
      <section class="surface span-12 card-section">
        <div class="bento-grid-mini">
          <div class="bento-card">
            <div class="bento-icon"><el-icon><VideoCamera /></el-icon></div>
            <div class="bento-content">
              <strong>{{ videos.length }}</strong>
              <p>当前用户已导入的文章总数</p>
            </div>
          </div>
          <div class="bento-card bento-success">
            <div class="bento-icon"><el-icon><Select /></el-icon></div>
            <div class="bento-content">
              <strong>{{ readyCount }}</strong>
              <p>状态为 SUCCESS，可直接创建会话</p>
            </div>
          </div>
          <div class="bento-card bento-danger">
            <div class="bento-icon"><el-icon><Warning /></el-icon></div>
            <div class="bento-content">
              <strong>{{ failedCount }}</strong>
              <p>状态为 FAILED，建议查看失败原因</p>
            </div>
          </div>
        </div>
      </section>

      <section class="surface span-12 card-section">
        <div class="card-header">
          <div>
            <div class="eyebrow">Library</div>
            <h2>文章库</h2>
            <p class="card-caption">建议先看状态是否可用，再选择进入问答或继续补充更多文章。</p>
          </div>
          <div class="toolbar">
            <RouterLink to="/import">
              <el-button type="primary">继续导入文章</el-button>
            </RouterLink>
          </div>
        </div>

        <el-alert v-if="inlineError" class="alert-inline" type="error" :title="inlineError" show-icon />

        <div v-if="filteredVideos.length" class="bento-grid">
          <article v-for="video in filteredVideos" :key="video.id" class="bento-item bento-video">
            <div class="bento-item-header">
              <span class="badge-inline code-text">{{ video.sourceId || video.bvid }}</span>
              <StatusPill :label="statusMeta(video.status).label" :tone="statusMeta(video.status).tone" />
            </div>

            <div class="bento-item-content">
              <h3 class="bento-title">{{ video.title }}</h3>
              <p class="bento-desc">{{ clampText(video.description, 180) || "暂无文章简介" }}</p>
            </div>

            <div class="bento-item-meta top-gap">
              <span class="meta-label">
                <el-icon><Document /></el-icon> 内容片段 {{ video.chunkCount ?? "--" }}
              </span>
              <span class="meta-label">
                <el-icon><Clock /></el-icon> {{ formatDateTime(video.importTime) }}
              </span>
            </div>

            <div class="bento-item-actions hover-reveal">
              <el-button class="action-btn" @click="showVideoDetail(video.id)">详情</el-button>
              <el-button
                class="action-btn"
                type="primary"
                plain
                :disabled="video.status !== 'SUCCESS'"
                @click="createSingleSession(video)"
              >
                进入问答
              </el-button>
              <el-button class="action-btn" plain @click="openRebuildDialog(video)">重建索引</el-button>
              <el-button class="action-btn" type="danger" plain @click="deleteVideo(video)">删除</el-button>
            </div>

            <el-alert
              v-if="video.status === 'FAILED' && video.failReason"
              class="top-gap bento-alert"
              type="warning"
              :title="video.failReason"
              show-icon
              :closable="false"
            />
          </article>
        </div>

        <EmptyState
          v-else
          badge="ARTICLES"
          :title="videos.length ? '没有匹配的文章' : '知识库空空如也'"
          :description="videos.length ? '尝试更换搜索关键词。' : '第一步：请先导入一篇 CSDN 文章。系统会自动提取正文并进行向量化处理，完成后你就可以在这里看到它。'"
        >
          <RouterLink to="/import">
            <el-button type="primary" size="large">
              立即导入第一篇文章
              <el-icon class="el-icon--right"><Download /></el-icon>
            </el-button>
          </RouterLink>
        </EmptyState>
      </section>
    </div>

    <el-drawer v-model="detailVisible" title="文章详情" size="520px">
      <div v-loading="detailLoading" class="stack drawer-content">
        <template v-if="selectedVideo">
          <div class="surface-strong card-section bento-drawer-header">
            <div class="eyebrow">Article Detail</div>
            <h3>{{ selectedVideo.title }}</h3>
            <p class="card-caption">{{ selectedVideo.description || "暂无简介" }}</p>
          </div>
          <div class="surface-strong card-section">
            <div class="stack meta-list">
              <div class="meta-item">
                <strong>文章标识：</strong>
                <span class="code-text">{{ selectedVideo.sourceId || selectedVideo.bvid }}</span>
              </div>
              <div v-if="selectedVideo.sourceUrl" class="meta-item">
                <strong>来源链接：</strong>
                <a :href="selectedVideo.sourceUrl" target="_blank" rel="noreferrer">{{ selectedVideo.sourceUrl }}</a>
              </div>
              <div class="meta-item">
                <strong>状态：</strong>
                <StatusPill :label="statusMeta(selectedVideo.status).label" :tone="statusMeta(selectedVideo.status).tone" />
              </div>
              <div class="meta-item">
                <strong>分片数：</strong>
                <span>{{ selectedVideo.chunkCount ?? "--" }}</span>
              </div>
              <div class="meta-item">
                <strong>导入时间：</strong>
                <span>{{ formatDateTime(selectedVideo.importTime) }}</span>
              </div>
              <div v-if="selectedVideo.failReason" class="meta-item error-item">
                <strong>失败原因：</strong>
                <span class="text-danger">{{ selectedVideo.failReason }}</span>
              </div>
            </div>
          </div>
        </template>
      </div>
    </el-drawer>

    <el-dialog v-model="rebuildDialogVisible" title="重建文章索引" width="560px">
      <div class="stack">
        <p class="card-caption">
          将基于当前保存的 CSDN 文章链接重新抓取正文、重新切分并重写向量索引，用于让历史数据也应用最新的
          chunk overlap 策略。
        </p>
        <div v-if="rebuildTarget" class="surface-strong card-section">
          <div class="meta-item"><strong>目标文章：</strong><span>{{ rebuildTarget.title }}</span></div>
          <div class="meta-item"><strong>文章标识：</strong><span class="code-text">{{ rebuildTarget.sourceId || rebuildTarget.bvid }}</span></div>
          <div v-if="rebuildTarget.sourceUrl" class="meta-item"><strong>来源链接：</strong><span class="code-text">{{ rebuildTarget.sourceUrl }}</span></div>
        </div>
        <el-alert v-if="rebuildInlineError" type="error" :title="rebuildInlineError" show-icon :closable="false" />
      </div>
      <template #footer>
        <el-button @click="closeRebuildDialog">取消</el-button>
        <el-button type="primary" :loading="rebuildSubmitting" @click="submitRebuild">开始重建</el-button>
      </template>
    </el-dialog>

    <!-- Welcome & Guide Dialog -->
    <el-dialog
      v-model="guideVisible"
      title="欢迎使用 RAG CSDN！"
      width="640px"
      :close-on-click-modal="false"
      class="guide-dialog"
    >
      <div class="guide-welcome-content">
        <div class="guide-hero">
          <div class="nav-mark">RC</div>
          <div class="hero-text">
            <h3>开启你的智能文章知识库</h3>
            <p>只需简单三步，即可将 CSDN 社区文章转化为可随时提问的专家级助手。</p>
          </div>
        </div>

        <div class="guide-steps-vertical">
          <div class="guide-step">
            <div class="step-num">01</div>
            <div class="step-info">
              <h4>导入文章</h4>
              <p>点击左侧“导入文章”，输入 CSDN 文章链接。系统会自动抓取正文并进行向量化建模。</p>
            </div>
          </div>
          <div class="guide-step">
            <div class="step-num">02</div>
            <div class="step-info">
              <h4>查看状态</h4>
              <p>在“文章列表”中观察进度。当状态变为 <strong>SUCCESS</strong> 时，表示知识沉淀已完成。</p>
            </div>
          </div>
          <div class="guide-step">
            <div class="step-num">03</div>
            <div class="step-info">
              <h4>发起对话</h4>
              <p>点击“进入问答”，即可针对该文章（或全局库）发起对话。AI 会根据文章原文精准回答。</p>
            </div>
          </div>
        </div>

        <div class="guide-footer-tip">
          <el-icon><InfoFilled /></el-icon>
          <span>提示：我们已在侧边栏为您准备了显眼的“导入文章”入口。</span>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" size="large" @click="closeGuide" class="full-width">
          我准备好了，开始体验
        </el-button>
      </template>
    </el-dialog>
  </AppShell>
</template>

<script setup>
import { ElMessage, ElMessageBox } from "element-plus";
import { Search, VideoCamera, Select, Warning, Clock, Document, Download, InfoFilled } from "@element-plus/icons-vue";
import { computed, ref, onMounted } from "vue";
import { RouterLink, useRouter } from "vue-router";

import AppShell from "../components/AppShell.vue";
import EmptyState from "../components/EmptyState.vue";
import StatusPill from "../components/StatusPill.vue";
import { articlesApi } from "../api/articles";
import { sessionsApi } from "../api/sessions";
import { VIDEO_STATUS_META } from "../constants/options";
import { notifyError } from "../utils/error";
import { clampText, formatDateTime } from "../utils/format";

const router = useRouter();

const videos = ref([]);
const loading = ref(false);
const inlineError = ref("");
const keyword = ref("");
const detailVisible = ref(false);
const detailLoading = ref(false);
const selectedVideo = ref(null);
const guideVisible = ref(false);
const rebuildDialogVisible = ref(false);
const rebuildSubmitting = ref(false);
const rebuildInlineError = ref("");
const rebuildTarget = ref(null);

const GUIDE_STORAGE_KEY = "rag-csdn-guide-shown";
const LEGACY_GUIDE_STORAGE_KEY = "rag-bilibili-guide-shown";

function hasSeenGuide() {
  const currentValue = localStorage.getItem(GUIDE_STORAGE_KEY);
  if (currentValue) {
    return true;
  }

  const legacyValue = localStorage.getItem(LEGACY_GUIDE_STORAGE_KEY);
  if (legacyValue) {
    localStorage.setItem(GUIDE_STORAGE_KEY, legacyValue);
    localStorage.removeItem(LEGACY_GUIDE_STORAGE_KEY);
    return true;
  }

  return false;
}

onMounted(() => {
  if (!hasSeenGuide()) {
    guideVisible.value = true;
  }
});

function closeGuide() {
  guideVisible.value = false;
  localStorage.setItem(GUIDE_STORAGE_KEY, "true");
  localStorage.removeItem(LEGACY_GUIDE_STORAGE_KEY);
}

const filteredVideos = computed(() => {
  const query = keyword.value.toLowerCase();
  if (!query) {
    return videos.value;
  }
  return videos.value.filter(
    (video) =>
      video.title?.toLowerCase().includes(query) ||
      (video.sourceId || video.bvid || "").toLowerCase().includes(query)
  );
});

const readyCount = computed(() => videos.value.filter((item) => item.status === "SUCCESS").length);
const failedCount = computed(() => videos.value.filter((item) => item.status === "FAILED").length);

loadVideos();

function statusMeta(status) {
  return VIDEO_STATUS_META[status] || { label: status || "--", tone: "warning" };
}

async function loadVideos() {
  loading.value = true;
  inlineError.value = "";
  try {
    videos.value = await articlesApi.list();
  } catch (error) {
    inlineError.value = notifyError(error).message;
  } finally {
    loading.value = false;
  }
}

async function showVideoDetail(id) {
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    selectedVideo.value = await articlesApi.detail(id);
  } catch (error) {
    detailVisible.value = false;
    notifyError(error);
  } finally {
    detailLoading.value = false;
  }
}

async function createSingleSession(video) {
  try {
    const session = await sessionsApi.create({
      sessionType: "SINGLE_ARTICLE",
      articleId: video.id,
    });
    ElMessage.success("会话创建成功");
    await router.push({ name: "chat", params: { sessionId: session.id } });
  } catch (error) {
    notifyError(error);
  }
}

function openRebuildDialog(video) {
  rebuildTarget.value = video;
  rebuildInlineError.value = "";
  rebuildDialogVisible.value = true;
}

function closeRebuildDialog() {
  rebuildDialogVisible.value = false;
  rebuildInlineError.value = "";
  rebuildTarget.value = null;
}

async function submitRebuild() {
  if (!rebuildTarget.value) {
    rebuildInlineError.value = "未找到需要重建的文章。";
    return;
  }

  rebuildSubmitting.value = true;
  rebuildInlineError.value = "";
  try {
    await articlesApi.rebuild(rebuildTarget.value.id);
    ElMessage.success("已提交重建任务，请稍后刷新查看状态");
    closeRebuildDialog();
    await loadVideos();
  } catch (error) {
    rebuildInlineError.value = notifyError(error).message;
  } finally {
    rebuildSubmitting.value = false;
  }
}

async function deleteVideo(video) {
  try {
    await ElMessageBox.confirm(
      `确定删除《${video.title}》吗？该操作会联动删除向量数据和相关单文章会话。`,
      "删除文章",
      {
        confirmButtonText: "确认删除",
        cancelButtonText: "取消",
        type: "warning",
      }
    );
    await articlesApi.remove(video.id);
    ElMessage.success("文章已删除");
    await loadVideos();
  } catch (error) {
    if (error !== "cancel") {
      notifyError(error);
    }
  }
}
</script>

<style scoped>
/* Bento Grid Layouts */
.bento-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
}

.bento-grid-mini {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

/* Base Card Style */
.bento-card {
  background: var(--rb-panel);
  border: 1px solid var(--rb-border);
  border-radius: var(--rb-radius-lg);
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(8px);
}

.bento-card:hover {
  transform: translateY(-4px);
  border-color: var(--rb-accent);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.bento-icon {
  font-size: 1.5rem;
  color: var(--rb-accent);
  background: var(--rb-bg-strong);
  width: 56px;
  height: 56px;
  border-radius: var(--rb-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--rb-border);
}

.bento-content strong {
  font-family: var(--font-heading);
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--rb-text);
  line-height: 1;
  display: block;
  margin-bottom: 4px;
}

.bento-content p {
  margin: 0;
  color: var(--rb-text-soft);
  font-size: 0.9rem;
  font-weight: 500;
}

/* List Items (Videos) */
.bento-item {
  background: var(--rb-panel);
  border: 1px solid var(--rb-border);
  border-radius: var(--rb-radius-lg);
  padding: 24px;
  display: flex;
  flex-direction: column;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-height: 260px;
  backdrop-filter: blur(8px);
}

.bento-item:hover {
  transform: translateY(-4px);
  border-color: var(--rb-accent);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
}

.bento-title {
  font-family: var(--font-heading);
  font-size: 1.25rem;
  margin: 0 0 10px 0;
  line-height: 1.4;
  font-weight: 700;
  color: var(--rb-text);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.bento-desc {
  margin: 0;
  color: var(--rb-text-soft);
  font-size: 0.9rem;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.bento-item-meta {
  display: flex;
  gap: 20px;
  margin-top: auto;
  padding-top: 20px;
  border-top: 1px solid var(--rb-border);
}

.meta-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85rem;
  color: var(--rb-text-soft);
  font-weight: 500;
}

.bento-item-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: stretch;
  gap: 12px;
  margin-top: 16px;
}

.action-btn {
  flex: 1 1 150px;
}

.bento-alert {
  margin-top: 16px;
}

/* Hover reveal effect for actions */
@media (hover: hover) {
  .hover-reveal {
    opacity: 0.8;
    transition: opacity 0.2s;
  }

  .bento-item:hover .hover-reveal {
    opacity: 1;
  }
}

/* Drawer Detail Styling */
.drawer-content {
  color: var(--rb-text);
}

.bento-drawer-header {
  background: var(--rb-panel);
  border-color: var(--rb-border);
}

.bento-drawer-header h3 {
  color: var(--rb-text);
}

.meta-list {
  gap: 16px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--rb-border);
}

.meta-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.meta-item strong {
  color: var(--rb-text-muted);
  font-size: 0.85rem;
}

.meta-item span {
  font-size: 0.95rem;
  color: var(--rb-text);
}

.text-danger {
  color: #ef4444;
}

/* Guide Dialog Styling */
.guide-welcome-content {
  color: var(--rb-text);
  padding: 0 12px;
}

.guide-hero {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
}

.guide-hero .nav-mark {
  width: 56px;
  height: 56px;
  font-size: 1.5rem;
}

.guide-hero h3 {
  font-family: var(--font-heading);
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 4px;
}

.guide-hero p {
  color: var(--rb-text-soft);
  font-size: 1rem;
}

.guide-steps-vertical {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-bottom: 32px;
}

.guide-step {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.step-num {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--rb-bg-strong);
  border: 1px solid var(--rb-border);
  border-radius: 50%;
  color: var(--rb-accent);
  font-weight: 700;
  font-size: 0.85rem;
  flex-shrink: 0;
}

.step-info h4 {
  font-family: var(--font-heading);
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 4px;
}

.step-info p {
  color: var(--rb-text-soft);
  font-size: 0.95rem;
  line-height: 1.6;
}

.guide-footer-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: var(--rb-bg-strong);
  border-radius: var(--rb-radius-md);
  color: var(--rb-accent);
  font-size: 0.9rem;
  font-weight: 500;
}

.guide-dialog :deep(.el-dialog__footer) {
  padding-top: 0;
  padding-bottom: 32px;
  padding-left: 32px;
  padding-right: 32px;
}

.full-width {
  width: 100%;
}

@media (max-width: 768px) {
  .bento-grid-mini {
    grid-template-columns: 1fr;
  }

  .bento-grid {
    grid-template-columns: 1fr;
  }

  .action-btn {
    width: 100%;
  }
}
</style>
