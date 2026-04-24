<template>
  <header class="site-header" :class="{ 'is-scrolled': isScrolled }">
    <div class="header-container">
      <RouterLink to="/" class="header-brand">
        <div class="nav-mark">RB</div>
        <span class="logo-text">RAG CSDN</span>
      </RouterLink>

      <nav class="header-nav">
        <template v-if="!authStore.isAuthenticated">
          <a href="/#features" class="nav-link">核心能力</a>
          <a href="/#how-it-works" class="nav-link">业务流程</a>
          <RouterLink to="/login" class="nav-link">登录</RouterLink>
          <RouterLink to="/register" class="btn btn-primary btn-sm">注册账号</RouterLink>
        </template>
        <template v-else>
          <RouterLink to="/articles" class="nav-link">文章库</RouterLink>
          <RouterLink to="/sessions" class="nav-link">会话</RouterLink>
          <RouterLink to="/import" class="nav-link">导入</RouterLink>
          <RouterLink to="/articles" class="btn btn-primary btn-sm">进入工作台</RouterLink>
        </template>
        
        <button class="theme-toggle-header" @click="toggleTheme" title="切换主题">
          <el-icon v-if="theme === 'dark'"><Sunny /></el-icon>
          <el-icon v-else><Moon /></el-icon>
        </button>
      </nav>
    </div>
  </header>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from "vue";
import { RouterLink } from "vue-router";
import { Sunny, Moon } from "@element-plus/icons-vue";
import { useTheme } from "../composables/useTheme";
import { useAuthStore } from "../stores/auth";

const { theme, toggleTheme } = useTheme();
const authStore = useAuthStore();
const isScrolled = ref(false);

function handleScroll() {
  isScrolled.value = window.scrollY > 20;
}

onMounted(() => {
  window.addEventListener("scroll", handleScroll);
});

onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll);
});
</script>

<style scoped>
.site-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: 72px;
  display: flex;
  align-items: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-bottom: 1px solid transparent;
}

.site-header.is-scrolled {
  background: var(--rb-panel);
  height: 64px;
  border-bottom-color: var(--rb-border);
  backdrop-filter: blur(12px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.header-container {
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: var(--rb-text);
}

.nav-mark {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--rb-accent);
  border-radius: var(--rb-radius-md);
  font-family: var(--font-heading);
  font-weight: 700;
  font-size: 1rem;
  color: #fff;
  box-shadow: 0 0 15px rgba(var(--rb-accent-rgb), 0.2);
}

.logo-text {
  font-family: var(--font-heading);
  font-size: 1.15rem;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 32px;
}

.nav-link {
  text-decoration: none;
  color: var(--rb-text-soft);
  font-size: 0.95rem;
  font-weight: 500;
  transition: color 0.2s;
}

.nav-link:hover {
  color: var(--rb-text);
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 20px;
  border-radius: var(--rb-radius-md);
  font-size: 0.9rem;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.2s;
  cursor: pointer;
}

.btn-sm {
  padding: 6px 16px;
  font-size: 0.85rem;
}

.btn-primary {
  background: var(--rb-accent);
  color: #fff;
}

.btn-primary:hover {
  background: var(--rb-accent-strong);
  transform: translateY(-1px);
}

.theme-toggle-header {
  background: none;
  border: none;
  color: var(--rb-text-soft);
  cursor: pointer;
  display: flex;
  align-items: center;
  font-size: 1.25rem;
  padding: 4px;
  border-radius: 50%;
  transition: all 0.2s;
}

.theme-toggle-header:hover {
  background: var(--rb-bg-strong);
  color: var(--rb-text);
}

@media (max-width: 768px) {
  .header-nav .nav-link {
    display: none;
  }
}
</style>
