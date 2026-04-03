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
                <MarkdownContent
                  v-if="message.role === 'ASSISTANT'"
                  :content="message.content || '...'"
                  :streaming="streaming && message.localKey === pendingAssistantMessage?.localKey"
                  class="message-content"
                />
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
import { onBeforeUnmount, ref, nextTick } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";

import AppShell from "../components/AppShell.vue";
import EmptyState from "../components/EmptyState.vue";
import StatusPill from "../components/StatusPill.vue";
import MarkdownContent from "../components/MarkdownContent.vue";
import { messagesApi } from "../api/messages";
import { sessionsApi } from "../api/sessions";
import { MESSAGE_ROLE_META } from "../constants/options";
import { notifyError } from "../utils/error";
import { formatDateTime } from "../utils/format";
import { logger } from "../utils/logger";

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

let abortController = null;
let streamThrottleTimer = null;
const pendingAssistantMessage = ref(null);
let pendingAssistantDelta = "";

loadSession();

onBeforeUnmount(() => {
  abortStream();
  clearPendingStreamFrame();
});

function roleMeta(role) {
  return MESSAGE_ROLE_META[role] || { label: role || "UNKNOWN", tone: "warning" };
}

function withLocalKey(message) {
  return {
    localKey: message.id || `${message.role}-${Date.now()}-${Math.random().toString(16).slice(2)}`,
    ...message,
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
  if (streaming.value) return;

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
          flushPendingAssistantContent();
          hasCompleted = true;
          pendingAssistantMessage.value = patchMessage(assistantMessage.localKey, (current) => ({
            ...current,
            id: payload.assistantMessageId || current.id,
            content: payload.fullContent || current.content,
          }));
          logger.info("chat", "收到 end 事件", payload);
        },
        error(payload) {
          throw new Error(payload?.message || "流式回答出现异常。");
        },
      },
      abortController.signal
    );
    const latestMessages = await messagesApi.list(sessionId);
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

/* Base Message Item */
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

/* User Message */
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

/* Assistant Message */
.message-assistant {
  align-self: flex-start;
}

.message-assistant .message-bubble {
  background: transparent;
  border: none;
  padding: 0;
}

/* Composer */
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
  padding-right: 120px; /* Make room for button */
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

/* Meta Section */
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
</style>
