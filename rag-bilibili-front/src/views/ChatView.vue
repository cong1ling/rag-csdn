<template>
  <AppShell
    eyebrow="Streaming Chat"
    title="检索增强问答"
    subtitle="围绕当前会话持续提问。系统会结合你导入的内容逐步生成回答，你可以继续追问细节、总结观点或回顾重点。"
  >
    <template #header-actions>
      <div class="toolbar">
        <el-button @click="reloadSession" :loading="loading">刷新会话</el-button>
        <RouterLink to="/sessions">
          <el-button>返回会话列表</el-button>
        </RouterLink>
      </div>
    </template>

    <div class="page-grid chat-layout">
      <section class="surface span-8 card-section chat-main">
        <div class="card-header chat-header">
          <div>
            <div class="eyebrow">Conversation</div>
            <h2>{{ session?.videoTitle || "全部视频知识库" }}</h2>
            <p class="card-caption">
              {{
                session?.sessionType === "SINGLE_VIDEO"
                  ? "当前仅检索指定视频的字幕分片。"
                  : "当前检索当前用户全部已导入视频。"
              }}
            </p>
          </div>
          <StatusPill :label="streaming ? '生成中' : '可提问'" :tone="streaming ? 'warning' : 'success'" />
        </div>

        <el-alert
          v-if="inlineError"
          class="alert-inline"
          type="error"
          :title="inlineError"
          show-icon
          role="alert"
        />

        <div v-if="messages.length" class="message-list-container" ref="messageListRef">
          <div class="message-list">
            <article
              v-for="message in messages"
              :key="message.localKey"
              class="message-item"
              :class="message.role === 'USER' ? 'message-user' : 'message-assistant'"
            >
              <div class="message-meta">
                <span class="message-role">{{ roleMeta(message.role).label }}</span>
                <span class="muted time">{{ formatDateTime(message.createTime) }}</span>
              </div>

              <div class="message-bubble">
                <template v-if="message.role === 'ASSISTANT'">
                  <MarkdownContent
                    :content="message.content || '...'"
                    :streaming="streaming && message.localKey === pendingAssistantMessage?.localKey"
                    class="message-content"
                  />
                  <div v-if="message.responseMeta" class="assistant-insight">
                    <div class="assistant-insight-pills">
                      <StatusPill
                        :label="message.responseMeta.confidenceText"
                        :tone="message.responseMeta.confidenceTone"
                      />
                      <StatusPill :label="message.responseMeta.intentText" tone="info" />
                      <StatusPill v-if="message.responseMeta.knowledgeGap" label="信息缺口" tone="warning" />
                    </div>
                    <p class="assistant-insight-note" :class="`tone-${message.responseMeta.noteTone}`">
                      {{ message.responseMeta.note }}
                    </p>
                  </div>
                </template>
                <p v-else class="message-content user-text">{{ message.content || "..." }}</p>
              </div>
            </article>
          </div>
        </div>

        <EmptyState
          v-else
          badge="CHAT"
          title="还没有消息"
          description="先提出一个你真正关心的问题，例如视频核心观点、实现步骤、删除规则或知识总结。"
        />

        <div class="composer-container surface-strong">
          <div class="composer-header">
            <div>
              <h3>发送问题</h3>
            </div>
            <div class="toolbar">
              <el-button v-if="streaming" type="danger" plain size="small" @click="abortStream">停止生成</el-button>
            </div>
          </div>

          <el-form @submit.prevent="sendMessage" class="composer-form">
            <el-input
              v-model.trim="draft"
              type="textarea"
              :rows="3"
              resize="none"
              maxlength="2000"
              placeholder="例如：这个视频讲了什么？（按 Enter 发送，Shift + Enter 换行）"
              @keydown.enter.prevent.exact="sendMessage"
              class="composer-input"
            />
            <div class="composer-actions">
              <el-button type="primary" :loading="streaming" @click="sendMessage">
                发送 <el-icon class="el-icon--right"><Promotion /></el-icon>
              </el-button>
            </div>
          </el-form>
        </div>
      </section>

      <section class="surface span-4 card-section">
        <div class="card-header">
          <div>
            <div class="eyebrow">Session Meta</div>
            <h2>会话信息</h2>
          </div>
        </div>

        <div v-if="session" class="stack">
          <div class="surface-strong card-section">
            <div class="eyebrow">使用说明</div>
            <div class="stack meta-list">
              <div class="meta-item">
                <span class="meta-label">问答范围：</span>
                <span class="meta-value">{{ session.sessionType === "SINGLE_VIDEO" ? "单个视频" : "全部视频" }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">视频标题：</span>
                <span class="meta-value">{{ session.videoTitle || "全部视频知识库" }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">创建时间：</span>
                <span class="meta-value">{{ formatDateTime(session.createTime) }}</span>
              </div>
            </div>
          </div>

          <div class="surface-strong card-section">
            <div class="eyebrow">Conversation Memory</div>
            <div class="stack top-gap">
              <p v-if="session.conversationSummary" class="summary-text">{{ session.conversationSummary }}</p>
              <p v-else class="empty-hint">当前会话还没有摘要。多轮连续提问后，系统会自动压缩上下文并复用。</p>
              <div class="meta-list">
                <div class="meta-item">
                  <span class="meta-label">摘要更新时间：</span>
                  <span class="meta-value">{{ formatDateTime(session.summaryUpdateTime) }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="surface-strong card-section">
            <div class="eyebrow">Answer Signals</div>
            <div v-if="latestResponseMeta" class="stack top-gap signal-list">
              <div class="assistant-insight-pills">
                <StatusPill :label="latestResponseMeta.confidenceText" :tone="latestResponseMeta.confidenceTone" />
                <StatusPill :label="latestResponseMeta.intentText" tone="info" />
                <StatusPill v-if="latestResponseMeta.summaryUsed" label="用了摘要记忆" tone="success" />
              </div>

              <div class="signal-grid">
                <div class="signal-row">
                  <span class="signal-label">检索策略</span>
                  <span class="signal-value">{{ latestResponseMeta.intentHint }}</span>
                </div>
                <div class="signal-row">
                  <span class="signal-label">命中文档</span>
                  <span class="signal-value">{{ latestResponseMeta.sourceCount ?? "--" }}</span>
                </div>
                <div class="signal-row">
                  <span class="signal-label">置信分</span>
                  <span class="signal-value">{{ latestResponseMeta.confidenceDisplay }}</span>
                </div>
              </div>

              <div v-if="latestResponseMeta.rewrittenQuery" class="signal-block">
                <div class="signal-label">改写后的查询</div>
                <div class="signal-query">{{ latestResponseMeta.rewrittenQuery }}</div>
              </div>

              <div class="signal-banner" :class="`tone-${latestResponseMeta.noteTone}`">
                {{ latestResponseMeta.note }}
              </div>
            </div>
            <p v-else class="empty-hint">完成一轮回答后，这里会显示查询理解、改写结果和回答可信度。</p>
          </div>

          <div class="surface-strong card-section">
            <div class="eyebrow">提问建议</div>
            <div class="stack top-gap tip-list">
              <div class="tip-item">先问“这个视频主要讲了什么”，快速了解大意。</div>
              <div class="tip-item">再问“有哪些关键步骤、限制或删除规则”，补齐细节。</div>
              <div class="tip-item">如果是全视频会话，适合做横向对比和总结归纳。</div>
              <div class="tip-item">回答不够聚焦时，可以换成更短、更具体的问题。</div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </AppShell>
</template>

<script setup>
import { ElMessage } from "element-plus";
import { Promotion } from "@element-plus/icons-vue";
import { computed, nextTick, onBeforeUnmount, ref } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";

import { messagesApi } from "../api/messages";
import { sessionsApi } from "../api/sessions";
import AppShell from "../components/AppShell.vue";
import EmptyState from "../components/EmptyState.vue";
import MarkdownContent from "../components/MarkdownContent.vue";
import StatusPill from "../components/StatusPill.vue";
import { MESSAGE_ROLE_META } from "../constants/options";
import { notifyError } from "../utils/error";
import { formatDateTime } from "../utils/format";
import { logger } from "../utils/logger";

const QUERY_INTENT_META = {
  DIRECT: {
    text: "直接检索",
    hint: "问题足够明确，直接进入召回与回答。",
  },
  AMBIGUOUS: {
    text: "模糊补全",
    hint: "问题偏短或语义不完整，系统先扩写再检索。",
  },
  BROAD: {
    text: "宽泛拆解",
    hint: "问题范围较大，系统会拆成多个子问题并行检索。",
  },
};

const CONFIDENCE_META = {
  HIGH: {
    text: "高置信",
    tone: "success",
  },
  MEDIUM: {
    text: "中置信",
    tone: "warning",
  },
  LOW: {
    text: "低置信",
    tone: "error",
  },
};

const route = useRoute();
const router = useRouter();
const sessionId = Number(route.params.sessionId);

const session = ref(null);
const messages = ref([]);
const loading = ref(false);
const streaming = ref(false);
const inlineError = ref("");
const draft = ref("");
const messageListRef = ref(null);
const pendingAssistantMessage = ref(null);
const messageMetaById = ref({});

let abortController = null;
let streamThrottleTimer = null;
let pendingAssistantDelta = "";

const latestResponseMeta = computed(() => {
  const matched = [...messages.value]
    .reverse()
    .find((item) => item.role === "ASSISTANT" && item.responseMeta);
  return matched?.responseMeta || null;
});

loadSession();

onBeforeUnmount(() => {
  abortStream();
  clearPendingStreamFrame();
});

function roleMeta(role) {
  return MESSAGE_ROLE_META[role] || { label: role || "UNKNOWN", tone: "warning" };
}

function getStoredResponseMeta(message) {
  if (!message?.id) {
    return null;
  }
  return messageMetaById.value[String(message.id)] || null;
}

function withLocalKey(message) {
  const responseMeta = getStoredResponseMeta(message);
  return {
    localKey: message.id || `${message.role}-${Date.now()}-${Math.random().toString(16).slice(2)}`,
    ...message,
    ...(responseMeta ? { responseMeta } : {}),
  };
}

function patchMessage(localKey, updater) {
  const index = messages.value.findIndex((item) => item.localKey === localKey);
  if (index < 0) {
    return null;
  }

  const current = messages.value[index];
  const next = typeof updater === "function" ? updater(current) : { ...current, ...updater };
  messages.value[index] = next;
  return next;
}

function rememberResponseMeta(messageId, responseMeta) {
  if (!messageId || !responseMeta) {
    return;
  }
  messageMetaById.value = {
    ...messageMetaById.value,
    [String(messageId)]: responseMeta,
  };
}

function buildResponseMeta(payload = {}) {
  const hasPayload =
    payload.queryIntent ||
    payload.rewrittenQuery ||
    payload.confidenceLabel ||
    payload.confidenceScore !== undefined ||
    payload.sourceCount !== undefined ||
    payload.knowledgeGap !== undefined ||
    payload.summaryUsed !== undefined;

  if (!hasPayload) {
    return null;
  }

  const queryIntent = String(payload.queryIntent || "DIRECT").toUpperCase();
  const confidenceLabel = String(payload.confidenceLabel || "MEDIUM").toUpperCase();
  const intentMeta = QUERY_INTENT_META[queryIntent] || QUERY_INTENT_META.DIRECT;
  const confidenceMeta = CONFIDENCE_META[confidenceLabel] || CONFIDENCE_META.MEDIUM;
  const score = Number(payload.confidenceScore);
  const normalizedScore = Number.isFinite(score) ? Math.max(0, Math.min(1, score)) : null;
  const knowledgeGap = Boolean(payload.knowledgeGap);
  const sourceCountValue = Number(payload.sourceCount);
  const sourceCount = Number.isFinite(sourceCountValue) ? sourceCountValue : null;
  const rewrittenQuery = String(payload.rewrittenQuery || "").trim();

  let note = "回答基于当前命中的内容生成，你可以继续追问具体细节。";
  let noteTone = "info";

  if (knowledgeGap || confidenceLabel === "LOW") {
    note = "当前回答命中信息偏少，仅供参考。建议把问题再缩小一点，或补充关键词继续提问。";
    noteTone = "warning";
  } else if (confidenceLabel === "HIGH") {
    note = "当前回答命中内容较充分，适合继续沿当前话题深挖。";
    noteTone = "success";
  }

  return {
    queryIntent,
    intentText: intentMeta.text,
    intentHint: intentMeta.hint,
    rewrittenQuery,
    confidenceLabel,
    confidenceText: confidenceMeta.text,
    confidenceTone: confidenceMeta.tone,
    confidenceScore: normalizedScore,
    confidenceDisplay: normalizedScore === null ? "--" : `${Math.round(normalizedScore * 100)}%`,
    sourceCount,
    knowledgeGap,
    summaryUsed: Boolean(payload.summaryUsed),
    note,
    noteTone,
  };
}

function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight;
    }
  });
}

async function loadSession() {
  if (!Number.isInteger(sessionId) || sessionId <= 0) {
    inlineError.value = "当前会话地址无效，已返回会话列表。";
    await router.replace({ name: "sessions" });
    return;
  }

  loading.value = true;
  inlineError.value = "";
  try {
    const [sessionDetail, messageList] = await Promise.all([
      sessionsApi.detail(sessionId),
      messagesApi.list(sessionId),
    ]);
    session.value = sessionDetail;
    messages.value = messageList.map((item) => withLocalKey(item));
    scrollToBottom();
  } catch (error) {
    inlineError.value = notifyError(error).message;
  } finally {
    loading.value = false;
  }
}

async function reloadSession() {
  await loadSession();
}

function abortStream() {
  if (abortController) {
    abortController.abort();
    abortController = null;
  }
  flushPendingAssistantContent();
  streaming.value = false;
}

function clearPendingStreamFrame() {
  if (!streamThrottleTimer) {
    return;
  }
  clearTimeout(streamThrottleTimer);
  streamThrottleTimer = null;
}

function flushPendingAssistantContent() {
  clearPendingStreamFrame();
  if (!pendingAssistantMessage.value || !pendingAssistantDelta) {
    pendingAssistantDelta = "";
    return;
  }

  pendingAssistantMessage.value = patchMessage(pendingAssistantMessage.value.localKey, (current) => ({
    ...current,
    content: `${current.content || ""}${pendingAssistantDelta}`,
  }));
  pendingAssistantDelta = "";
  scrollToBottom();
}

function scheduleAssistantContent(message, delta) {
  pendingAssistantMessage.value = message;
  pendingAssistantDelta += delta;

  if (streamThrottleTimer) {
    return;
  }

  streamThrottleTimer = setTimeout(() => {
    streamThrottleTimer = null;
    flushPendingAssistantContent();
  }, 100);
}

async function sendMessage() {
  if (streaming.value) {
    return;
  }

  inlineError.value = "";

  const currentDraft = draft.value.trim();
  if (!currentDraft) {
    return;
  }

  const previousDraft = draft.value;
  const userMessage = withLocalKey({
    id: null,
    role: "USER",
    content: currentDraft,
    createTime: new Date().toISOString(),
  });
  const assistantMessage = withLocalKey({
    id: null,
    role: "ASSISTANT",
    content: "",
    createTime: new Date().toISOString(),
  });

  messages.value.push(userMessage, assistantMessage);
  scrollToBottom();

  streaming.value = true;
  abortController = new AbortController();
  let hasStarted = false;
  let hasCompleted = false;

  try {
    await messagesApi.stream(
      sessionId,
      { content: currentDraft },
      {
        start(payload) {
          hasStarted = true;
          patchMessage(userMessage.localKey, (current) => ({
            ...current,
            id: payload.userMessageId || current.id,
          }));
          draft.value = "";
          logger.info("chat", "收到 start 事件", payload);
        },
        content(payload) {
          if (!payload.delta) {
            return;
          }
          scheduleAssistantContent(assistantMessage, payload.delta);
        },
        end(payload) {
          const responseMeta = buildResponseMeta(payload);
          flushPendingAssistantContent();
          hasCompleted = true;

          if (payload.assistantMessageId && responseMeta) {
            rememberResponseMeta(payload.assistantMessageId, responseMeta);
          }

          pendingAssistantMessage.value = patchMessage(assistantMessage.localKey, (current) => ({
            ...current,
            id: payload.assistantMessageId || current.id,
            content: payload.fullContent || current.content,
            ...(responseMeta ? { responseMeta } : {}),
          }));
          logger.info("chat", "收到 end 事件", payload);
        },
        error(payload) {
          throw new Error(payload?.message || "流式回答出现异常。");
        },
      },
      abortController.signal
    );

    const [latestSession, latestMessages] = await Promise.all([
      sessionsApi.detail(sessionId),
      messagesApi.list(sessionId),
    ]);
    session.value = latestSession;
    messages.value = latestMessages.map((item) => withLocalKey(item));
    ElMessage.success("回答生成完成");
  } catch (error) {
    flushPendingAssistantContent();
    if (error?.name === "AbortError") {
      if (!hasStarted) {
        messages.value = messages.value.filter(
          (item) => item.localKey !== userMessage.localKey && item.localKey !== assistantMessage.localKey
        );
        draft.value = previousDraft;
      } else if (!hasCompleted) {
        patchMessage(assistantMessage.localKey, (current) => ({
          ...current,
          content: current.content || "当前回答已停止显示，你可以刷新会话后继续查看。",
        }));
      }
      ElMessage.warning("已停止当前流式响应");
    } else {
      const normalized = notifyError(error);
      inlineError.value = normalized.message;
      if (!hasStarted) {
        messages.value = messages.value.filter(
          (item) => item.localKey !== userMessage.localKey && item.localKey !== assistantMessage.localKey
        );
        draft.value = previousDraft;
      } else {
        patchMessage(assistantMessage.localKey, (current) => ({
          ...current,
          content: current.content || "回答生成中断，请稍后刷新会话再试。",
        }));
      }
    }
  } finally {
    pendingAssistantMessage.value = null;
    pendingAssistantDelta = "";
    clearPendingStreamFrame();
    streaming.value = false;
    abortController = null;
    scrollToBottom();
  }
}
</script>

<style scoped>
.chat-layout {
  height: calc(100vh - 200px);
  min-height: 500px;
}

.chat-main {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0;
}

.chat-header {
  padding: 20px 24px;
  border-bottom: 1px solid var(--rb-border);
  margin-bottom: 0;
}

.message-list-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
  background: var(--rb-bg);
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.message-item {
  display: flex;
  flex-direction: column;
  max-width: 85%;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.message-role {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--rb-text);
}

.time {
  font-size: 0.75rem;
  color: var(--rb-text-muted);
}

.message-bubble {
  padding: 16px;
  border-radius: var(--rb-radius-md);
  position: relative;
  line-height: 1.6;
}

.message-user {
  align-self: flex-end;
}

.message-user .message-meta {
  flex-direction: row-reverse;
}

.message-user .message-bubble {
  background: var(--rb-panel-strong);
  border: 1px solid var(--rb-border);
  border-bottom-right-radius: 4px;
}

.user-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  color: var(--rb-text);
  font-size: 0.95rem;
}

.message-assistant {
  align-self: flex-start;
}

.message-assistant .message-bubble {
  background: transparent;
  border: none;
  padding: 0;
}

.assistant-insight {
  margin-top: 12px;
  padding: 12px 14px;
  border: 1px solid var(--rb-border);
  border-radius: var(--rb-radius-md);
  background: var(--rb-panel);
}

.assistant-insight-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.assistant-insight-note {
  margin: 10px 0 0;
  font-size: 0.82rem;
  line-height: 1.6;
  color: var(--rb-text-muted);
}

.tone-success {
  color: var(--rb-success);
}

.tone-info {
  color: var(--rb-text-soft);
}

.tone-warning {
  color: var(--rb-warning);
}

.composer-container {
  padding: 16px 24px;
  border-top: 1px solid var(--rb-border);
  background: var(--rb-panel);
  margin: 0;
  border-radius: 0 0 var(--rb-radius-md) var(--rb-radius-md);
}

.composer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.composer-header h3 {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--rb-text);
}

.composer-form {
  position: relative;
}

.composer-input :deep(.el-textarea__inner) {
  border-radius: var(--rb-radius-md);
  padding: 12px 16px;
  padding-right: 120px;
  font-size: 0.95rem;
  background: var(--rb-bg);
  border: 1px solid var(--rb-border);
  box-shadow: none;
}

.composer-input :deep(.el-textarea__inner:focus) {
  border-color: #3b82f6;
}

.composer-actions {
  position: absolute;
  right: 12px;
  bottom: 12px;
}

.meta-list {
  gap: 12px;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px dashed var(--rb-border);
}

.meta-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.meta-label {
  font-weight: 600;
  color: var(--rb-text-muted);
  font-size: 0.85rem;
}

.meta-value {
  font-size: 0.9rem;
  max-width: 60%;
  text-align: right;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--rb-text);
}

.summary-text,
.empty-hint {
  margin: 0;
  line-height: 1.7;
  font-size: 0.9rem;
}

.summary-text {
  color: var(--rb-text-soft);
}

.empty-hint {
  color: var(--rb-text-muted);
}

.signal-list {
  gap: 14px;
}

.signal-grid {
  display: grid;
  gap: 10px;
}

.signal-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 10px;
  border-bottom: 1px dashed var(--rb-border);
}

.signal-row:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

.signal-label {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--rb-text-muted);
}

.signal-value {
  flex: 1;
  text-align: right;
  font-size: 0.88rem;
  line-height: 1.5;
  color: var(--rb-text);
}

.signal-block {
  display: grid;
  gap: 8px;
}

.signal-query {
  padding: 10px 12px;
  border-radius: var(--rb-radius-sm);
  border: 1px solid var(--rb-border);
  background: var(--rb-bg);
  color: var(--rb-text-soft);
  font-family: var(--font-mono);
  font-size: 0.8rem;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.signal-banner {
  padding: 12px 14px;
  border-radius: var(--rb-radius-md);
  border: 1px solid var(--rb-border);
  background: var(--rb-bg);
  font-size: 0.85rem;
  line-height: 1.7;
}

.tip-list {
  gap: 12px;
}

.tip-item {
  font-size: 0.85rem;
  padding: 10px 14px;
  background: var(--rb-panel);
  border-radius: var(--rb-radius-sm);
  border-left: 2px solid var(--rb-border-hover, var(--rb-border));
  color: var(--rb-text-muted);
  line-height: 1.5;
}

@media (max-width: 1024px) {
  .chat-layout {
    height: auto;
  }

  .message-item {
    max-width: 95%;
  }
}

@media (max-width: 768px) {
  .signal-row,
  .meta-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .signal-value,
  .meta-value {
    max-width: 100%;
    text-align: left;
    white-space: normal;
  }

  .composer-input :deep(.el-textarea__inner) {
    padding-right: 16px;
    min-height: 120px;
  }

  .composer-actions {
    position: static;
    margin-top: 12px;
  }
}
</style>
