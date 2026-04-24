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
          <h1 class="auth-title">创建账号</h1>
          <p class="auth-description">开始构建你的文章知识库</p>
        </div>

        <el-alert v-if="inlineError" class="auth-alert" type="error" :title="inlineError" show-icon />

        <el-form :model="form" class="auth-form" @submit.prevent="handleSubmit">
          <el-form-item label="用户名">
            <el-input
              v-model.trim="form.username"
              placeholder="3-50 个字符"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item label="密码">
            <el-input
              v-model.trim="form.password"
              type="password"
              show-password
              placeholder="6-20 个字符"
              size="large"
              :prefix-icon="Lock"
            />
          </el-form-item>

          <el-form-item label="确认密码">
            <el-input
              v-model.trim="form.confirmPassword"
              type="password"
              show-password
              placeholder="再次输入密码"
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
            立即注册
          </el-button>
        </el-form>

        <div class="auth-footer">
          <span class="auth-footer-text">已经有账号？</span>
          <RouterLink to="/login" class="auth-footer-link">立即登录</RouterLink>
        </div>
      </div>

      <div class="auth-features">
        <div class="feature-item surface">
          <div class="feature-icon"><el-icon><Lock /></el-icon></div>
          <div class="feature-text">
            <strong>数据隔离</strong>
            <span>独立的用户数据空间</span>
          </div>
        </div>
        <div class="feature-item surface">
          <div class="feature-icon"><el-icon><Lightning /></el-icon></div>
          <div class="feature-text">
            <strong>快速开始</strong>
            <span>注册后立即使用</span>
          </div>
        </div>
        <div class="feature-item surface">
          <div class="feature-icon"><el-icon><Files /></el-icon></div>
          <div class="feature-text">
            <strong>永久保存</strong>
            <span>数据长期保存</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from "element-plus";
import { User, Lock, Lightning, Files } from "@element-plus/icons-vue";
import { reactive, ref } from "vue";
import { RouterLink, useRouter } from "vue-router";

import { useAuthStore } from "../stores/auth";
import { useTheme } from "../composables/useTheme";
import { notifyError } from "../utils/error";

import Background3D from "../components/Background3D.vue";
import SiteHeader from "../components/SiteHeader.vue";

const { theme, toggleTheme } = useTheme();

const router = useRouter();
const authStore = useAuthStore();

const inlineError = ref("");
const form = reactive({
  username: "",
  password: "",
  confirmPassword: "",
});

async function handleSubmit() {
  inlineError.value = "";

  if (form.username.length < 3 || form.username.length > 50) {
    inlineError.value = "用户名长度必须在 3-50 之间。";
    return;
  }
  if (form.password.length < 6 || form.password.length > 20) {
    inlineError.value = "密码长度必须在 6-20 之间。";
    return;
  }
  if (form.password !== form.confirmPassword) {
    inlineError.value = "两次输入的密码不一致。";
    return;
  }

  try {
    await authStore.register({
      username: form.username,
      password: form.password,
    });
    ElMessage.success("注册成功，请登录");
    await router.push({ name: "login" });
  } catch (error) {
    inlineError.value = notifyError(error).message;
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  position: relative;
  overflow: hidden;
  isolation: isolate;
  background: var(--rb-bg);
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

.auth-header {
  text-align: center;
  margin-bottom: 2.5rem;
}

.auth-logo {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  text-decoration: none;
  margin-bottom: 1.5rem;
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

.auth-alert {
  margin-bottom: 1.5rem;
}

.auth-form :deep(.el-form-item__label) {
  color: var(--rb-text);
  font-weight: 500;
  padding-bottom: 8px;
}

.auth-submit {
  margin-top: 1rem;
}

.auth-footer {
  text-align: center;
  padding-top: 1.5rem;
  margin-top: 1.5rem;
  border-top: 1px solid var(--rb-border);
}

.auth-footer-text {
  color: var(--rb-text-soft);
  font-size: 0.875rem;
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
}

.auth-features {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
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

.full-width {
  width: 100%;
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
