<template>
  <div class="auth-page">
    <SiteHeader />
    <Background3D />
    
    <div class="auth-container">
      <div class="auth-card surface-strong">
        <div class="auth-header">
          <RouterLink to="/" class="auth-logo">
            <span class="logo-icon">RB</span>
            <span class="logo-text">RAG CSDN</span>
          </RouterLink>
          <h1 class="auth-title">登录工作台</h1>
          <p class="auth-description">开始管理你的文章知识库</p>
        </div>

        <el-alert v-if="inlineError" class="auth-alert" type="error" :title="inlineError" show-icon />

        <el-form :model="form" class="auth-form" @submit.prevent="handleSubmit">
          <el-form-item label="用户名">
            <el-input
              v-model.trim="form.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item label="密码">
            <el-input
              v-model.trim="form.password"
              type="password"
              show-password
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
            />
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            class="auth-submit full-width"
            :loading="authStore.loading"
            @click="handleSubmit"
          >
            立即登录
          </el-button>
        </el-form>

        <div class="auth-footer">
          <span class="auth-footer-text">还没有账号？</span>
          <RouterLink to="/register" class="auth-footer-link">立即注册</RouterLink>
        </div>

        <div v-if="authStore.canUseDeveloperEntry" class="auth-dev">
          <el-divider>开发模式</el-divider>
          <el-button
            size="small"
            class="full-width"
            :loading="authStore.loading"
            @click="handleDeveloperEntry"
          >
            进入开发模式
          </el-button>
        </div>
      </div>

      <div class="auth-features">
        <div class="feature-item surface">
          <div class="feature-icon"><el-icon><ChatLineRound /></el-icon></div>
          <div class="feature-text">
            <strong>智能问答</strong>
            <span>AI驱动的文章内容检索</span>
          </div>
        </div>
        <div class="feature-item surface">
          <div class="feature-icon"><el-icon><Collection /></el-icon></div>
          <div class="feature-text">
            <strong>知识管理</strong>
            <span>构建个人文章知识库</span>
          </div>
        </div>
        <div class="feature-item surface">
          <div class="feature-icon"><el-icon><Position /></el-icon></div>
          <div class="feature-text">
            <strong>快速上手</strong>
            <span>3步开始使用</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from "element-plus";
import { User, Lock, Sunny, Moon, ChatLineRound, Collection, Position } from "@element-plus/icons-vue";
import { reactive, ref } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import Background3D from "../components/Background3D.vue";
import SiteHeader from "../components/SiteHeader.vue";

import { useAuthStore } from "../stores/auth";
import { useTheme } from "../composables/useTheme";
import { notifyError } from "../utils/error";

const { theme, toggleTheme } = useTheme();

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const inlineError = ref("");
const form = reactive({
  username: "",
  password: "",
});

async function handleSubmit() {
  inlineError.value = "";

  if (!form.username || !form.password) {
    inlineError.value = "用户名和密码不能为空。";
    return;
  }

  try {
    await authStore.login(form);
    ElMessage.success("登录成功");
    await router.push(String(route.query.redirect || "/articles"));
  } catch (error) {
    inlineError.value = notifyError(error).message;
  }
}

async function handleDeveloperEntry() {
  inlineError.value = "";
  try {
    await authStore.enterDeveloperMode();
    ElMessage.success("已进入开发模式");
    await router.push(String(route.query.redirect || "/articles"));
  } catch (error) {
    inlineError.value = notifyError(error).message;
  }
}
</script>

<style scoped>
.theme-toggle-fab {
  position: fixed;
  top: 2rem;
  right: 2rem;
  z-index: 1000;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--rb-panel-strong);
  border: 1px solid var(--rb-border);
  border-radius: var(--rb-radius-md);
  color: var(--rb-text);
  font-size: 1.25rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.theme-toggle-fab:hover {
  border-color: var(--rb-border-hover);
  background: var(--rb-panel);
}

.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  position: relative;
  overflow: hidden;
  isolation: isolate;
}

.auth-container {
  width: 100%;
  max-width: 1000px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
  align-items: center;
  position: relative;
  z-index: 1;
}

.auth-card {
  background: var(--rb-panel);
  border: 1px solid var(--rb-border);
  border-radius: var(--rb-radius-lg);
  padding: 3rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(12px);
}

.auth-logo {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  text-decoration: none;
  margin-bottom: 2rem;
}

.logo-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--rb-accent);
  border-radius: var(--rb-radius-md);
  font-weight: 700;
  font-size: 1.25rem;
  color: #ffffff;
  box-shadow: 0 0 20px rgba(var(--rb-accent-rgb), 0.3);
}

.logo-text {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--rb-text);
  letter-spacing: -0.02em;
  font-family: var(--font-heading);
}

.auth-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--rb-text);
  margin-bottom: 0.5rem;
  letter-spacing: -0.02em;
  font-family: var(--font-heading);
}

.auth-description {
  font-size: 1rem;
  color: var(--rb-text-soft);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 1.25rem;
  padding: 1.5rem;
  background: var(--rb-panel);
  border: 1px solid var(--rb-border);
  border-radius: var(--rb-radius-lg);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(8px);
}

.feature-item:hover {
  transform: translateX(8px);
  border-color: var(--rb-accent);
  background: var(--rb-panel-strong);
}

.feature-icon {
  font-size: 1.5rem;
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color-light);
  border-radius: var(--rb-radius-md);
  color: var(--rb-accent);
  flex-shrink: 0;
}

.feature-text strong {
  display: block;
  color: var(--rb-text);
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 2px;
}

.feature-text span {
  color: var(--rb-text-soft);
  font-size: 0.9rem;
}

.auth-footer-link {
  color: var(--rb-accent);
  text-decoration: none;
  font-weight: 600;
  margin-left: 0.5rem;
  transition: opacity 0.2s;
}

.auth-footer-link:hover {
  opacity: 0.8;
  text-decoration: none;
}

.auth-form :deep(.el-input__wrapper) {
  background: var(--rb-bg-strong);
  border: 1px solid var(--rb-border);
  box-shadow: none;
  border-radius: var(--rb-radius-md);
  padding: 0 16px;
  transition: all 0.2s;
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--rb-accent);
  background: var(--rb-bg);
}

@media (max-width: 768px) {
  .auth-container {
    grid-template-columns: 1fr;
    gap: 2rem;
  }

  .auth-features {
    display: none;
  }

  .auth-card {
    padding: 2rem;
  }
}
</style>
