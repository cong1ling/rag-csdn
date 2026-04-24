<template>
  <AppShell
    eyebrow="Sessions"
    title="历史会话管理"
    subtitle="你可以为单篇文章创建专注型会话，也可以创建覆盖全部资料的综合会话。选好范围后，继续深入提问即可。"
  >
    <template #header-actions>
      <div class="toolbar">
        <el-button type="primary" @click="openCreateDialog({ sessionType: 'ALL_ARTICLES' })">
          全部会话
          <el-icon class="el-icon--right"><Plus /></el-icon>
        </el-button>
        <el-button @click="openCreateDialog({ sessionType: 'SINGLE_ARTICLE' })">单文章会话</el-button>
        <el-button @click="loadData" :loading="loading">刷新</el-button>
      </div>
    </template>

    <div class="page-grid">
      <section class="surface span-4 card-section">
        <div class="card-header">
          <div>
            <div class="eyebrow">Session Stats</div>
            <h2>概览</h2>
          </div>
        </div>
        <div class="bento-grid-mini">
          <div class="bento-card bento-primary">
            <div class="bento-icon"><el-icon><ChatDotRound /></el-icon></div>
            <div class="bento-content">
              <strong>{{ sessions.length }}</strong>
              <p>全部会话数量</p>
            </div>
          </div>
          <div class="bento-card">
            <div class="bento-content">
              <strong>{{ singleCount }}</strong>
              <p>单文章会话</p>
            </div>
          </div>
          <div class="bento-card">
            <div class="bento-content">
              <strong>{{ allArticlesCount }}</strong>
              <p>全部会话</p>
            </div>
          </div>
          <div class="bento-card">
            <div class="bento-content">
              <strong>{{ summarizedCount }}</strong>
              <p>已有摘要会话</p>
            </div>
          </div>
        </div>
      </section>

      <section class="surface span-8 card-section">
        <div class="card-header">
          <div>
            <div class="eyebrow">Conversations</div>
            <h2>会话列表</h2>
            <p class="card-caption">如果你想聚焦某一篇文章，就创建单文章会话；如果想跨文章比较或总结，就创建全部会话。</p>
          </div>
          <el-input v-model.trim="keyword" placeholder="搜索标题或摘要" clearable class="toolbar-search">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <el-alert v-if="inlineError" class="alert-inline" type="error" :title="inlineError" show-icon />

        <div v-if="filteredSessions.length" class="bento-grid">
          <article v-for="session in filteredSessions" :key="session.id" class="bento-item">
            <div class="bento-item-header">
              <div class="session-tags">
                <StatusPill
                  :label="session.sessionType === 'SINGLE_ARTICLE' ? '单文章' : '全部会话'"
                  :tone="session.sessionType === 'SINGLE_ARTICLE' ? 'success' : 'warning'"
                />
                <StatusPill v-if="session.conversationSummary" label="已生成摘要" tone="info" />
              </div>
              <span class="muted text-sm">{{ formatDateTime(session.createTime) }}</span>
            </div>

            <div class="bento-item-content">
              <h3 class="bento-title">{{ session.articleTitle || "全部文章知识库" }}</h3>
              <p class="bento-desc">
                {{
                  session.sessionType === "SINGLE_ARTICLE"
                    ? "检索范围限定为单篇文章。"
                    : "检索范围限定为当前用户导入的全部文章。"
                }}
              </p>

              <div class="summary-panel">
                <div class="summary-label">会话摘要</div>
                <p v-if="session.conversationSummary" class="summary-text">{{ session.conversationSummary }}</p>
                <p v-else class="summary-empty">还没有摘要。进入对话并完成几轮问答后，系统会自动沉淀上下文重点。</p>
              </div>

              <div class="summary-foot">
                <span class="muted text-sm">摘要更新：{{ formatDateTime(session.summaryUpdateTime) }}</span>
              </div>
            </div>

            <div class="bento-item-actions hover-reveal">
              <el-button type="primary" class="action-btn" @click="openChat(session.id)">进入对话</el-button>
              <el-button type="danger" plain class="action-btn" @click="removeSession(session)">删除</el-button>
            </div>
          </article>
        </div>

        <EmptyState
          v-else
          badge="SESSIONS"
          title="开启你的第一次对话"
          :description="availableArticles.length ? '文章已就绪。现在你可以创建一个单文章会话来深入探讨细节，或创建一个全部会话来进行全局检索。' : '你需要先导入文章并等待处理成功，然后才能在这里创建会话并开始 AI 对话。'"
        >
          <el-button
            v-if="availableArticles.length"
            type="primary"
            size="large"
            @click="openCreateDialog({ sessionType: 'ALL_ARTICLES' })"
          >
            立即创建全部会话
            <el-icon class="el-icon--right"><Plus /></el-icon>
          </el-button>
          <RouterLink v-else to="/import">
            <el-button type="primary" size="large">
              去导入第一篇文章
              <el-icon class="el-icon--right"><Download /></el-icon>
            </el-button>
          </RouterLink>
        </EmptyState>
      </section>
    </div>

    <SessionDialog
      v-model="dialogVisible"
      :articles="availableArticles"
      :loading="creating"
      :preset-article-id="presetArticleId"
      :preset-session-type="presetSessionType"
      @submit="handleCreateSession"
    />
  </AppShell>
</template>

<script setup>
import { ElMessage, ElMessageBox } from "element-plus";
import { ChatDotRound, Download, Plus, Search } from "@element-plus/icons-vue";
import { computed, ref } from "vue";
import { RouterLink, useRouter } from "vue-router";

import { sessionsApi } from "../api/sessions";
import { articlesApi } from "../api/articles";
import AppShell from "../components/AppShell.vue";
import EmptyState from "../components/EmptyState.vue";
import SessionDialog from "../components/SessionDialog.vue";
import StatusPill from "../components/StatusPill.vue";
import { notifyError } from "../utils/error";
import { formatDateTime } from "../utils/format";

const router = useRouter();

const sessions = ref([]);
const availableArticles = ref([]);
const loading = ref(false);
const creating = ref(false);
const inlineError = ref("");
const dialogVisible = ref(false);
const presetArticleId = ref(null);
const presetSessionType = ref("ALL_ARTICLES");
const keyword = ref("");

const filteredSessions = computed(() => {
  const query = keyword.value.toLowerCase();
  if (!query) {
    return sessions.value;
  }
  return sessions.value.filter((item) => {
    const title = item.articleTitle || "全部文章知识库";
    const summary = item.conversationSummary || "";
    return `${title} ${summary}`.toLowerCase().includes(query);
  });
});

const singleCount = computed(() => sessions.value.filter((item) => item.sessionType === "SINGLE_ARTICLE").length);
const allArticlesCount = computed(() => sessions.value.filter((item) => item.sessionType === "ALL_ARTICLES").length);
const summarizedCount = computed(() => sessions.value.filter((item) => item.conversationSummary).length);

loadData();

async function loadData() {
  loading.value = true;
  inlineError.value = "";

  try {
    const [sessionList, articleList] = await Promise.all([sessionsApi.list(), articlesApi.list()]);
    sessions.value = sessionList;
    availableArticles.value = articleList.filter((item) => item.status === "SUCCESS");
  } catch (error) {
    inlineError.value = notifyError(error).message;
  } finally {
    loading.value = false;
  }
}

function openCreateDialog({ articleId = null, sessionType = "ALL_ARTICLES" } = {}) {
  presetArticleId.value = articleId;
  presetSessionType.value = articleId ? "SINGLE_ARTICLE" : sessionType;
  dialogVisible.value = true;
}

async function handleCreateSession(payload) {
  creating.value = true;
  try {
    const session = await sessionsApi.create(payload);
    ElMessage.success("会话创建成功");
    dialogVisible.value = false;
    await loadData();
    await router.push({ name: "chat", params: { sessionId: session.id } });
  } catch (error) {
    notifyError(error);
  } finally {
    creating.value = false;
  }
}

async function removeSession(session) {
  try {
    await ElMessageBox.confirm("确定删除这个会话吗？", "删除会话", {
      confirmButtonText: "确认删除",
      cancelButtonText: "取消",
      type: "warning",
    });
    await sessionsApi.remove(session.id);
    ElMessage.success("会话已删除");
    await loadData();
  } catch (error) {
    if (error !== "cancel") {
      notifyError(error);
    }
  }
}

async function openChat(sessionId) {
  await router.push({ name: "chat", params: { sessionId } });
}
</script>

<style scoped>
.bento-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.bento-grid-mini {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.bento-card {
  background: var(--rb-panel-strong);
  border: 1px solid var(--rb-border);
  border-radius: var(--rb-radius-md);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: border-color 0.2s;
}

.bento-card:hover {
  border-color: var(--rb-border-hover);
}

.bento-primary {
  background: var(--rb-panel);
  border-color: var(--rb-border-hover);
}

.bento-icon {
  font-size: 2rem;
  color: var(--rb-text-muted);
  background: var(--rb-bg);
  width: 56px;
  height: 56px;
  border-radius: var(--rb-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--rb-border);
}

.bento-content strong {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--rb-text);
  line-height: 1;
  display: block;
  margin-bottom: 4px;
}

.bento-content p {
  margin: 0;
  color: var(--rb-text-muted);
  font-size: 0.875rem;
}

.bento-item {
  background: var(--rb-panel-strong);
  border: 1px solid var(--rb-border);
  border-radius: var(--rb-radius-md);
  padding: 20px;
  display: flex;
  flex-direction: column;
  transition: border-color 0.2s, box-shadow 0.2s;
  min-height: 260px;
}

.bento-item:hover {
  border-color: var(--rb-border-hover);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.bento-item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.session-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.bento-item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.bento-title {
  font-size: 1.125rem;
  margin: 0 0 8px 0;
  line-height: 1.4;
  font-weight: 600;
  color: var(--rb-text);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.bento-desc {
  margin: 0;
  color: var(--rb-text-muted);
  font-size: 0.875rem;
  line-height: 1.5;
}

.summary-panel {
  margin-top: 18px;
  padding: 14px;
  border: 1px solid var(--rb-border);
  border-radius: var(--rb-radius-md);
  background: var(--rb-bg);
}

.summary-label {
  margin-bottom: 8px;
  color: var(--rb-text-soft);
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.summary-text,
.summary-empty {
  margin: 0;
  font-size: 0.875rem;
  line-height: 1.7;
}

.summary-text {
  color: var(--rb-text);
}

.summary-empty {
  color: var(--rb-text-muted);
}

.summary-foot {
  margin-top: 12px;
}

.bento-item-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: stretch;
  gap: 12px;
  margin-top: 20px;
}

.action-btn {
  flex: 1 1 160px;
}

.text-sm {
  font-size: 0.875rem;
  color: var(--rb-text-muted);
}

@media (hover: hover) {
  .hover-reveal .action-btn {
    opacity: 0.8;
    transition: opacity 0.2s;
  }

  .bento-item:hover .hover-reveal .action-btn {
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .bento-item-header {
    flex-direction: column;
  }

  .action-btn {
    width: 100%;
  }
}
</style>
