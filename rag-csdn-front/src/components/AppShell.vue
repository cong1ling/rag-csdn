<template>
  <div class="app-shell">
    <div class="app-layout">
      <!-- Solid Sidebar -->
      <aside class="app-sidebar">
        <RouterLink to="/" class="nav-brand">
          <div class="nav-mark">RC</div>
          <div class="nav-brand-text">
            <strong>RAG CSDN</strong>
            <div class="nav-brand-subtitle">Enterprise Workspace</div>
          </div>
        </RouterLink>

        <div class="user-card">
          <div class="user-avatar">
            {{ (authStore.user?.username || "U")[0].toUpperCase() }}
          </div>
          <div class="user-info">
            <strong class="user-name">{{ authStore.user?.username || "未登录" }}</strong>
            <span class="user-status">
              {{ authStore.isDeveloperMode ? "开发模式" : "已登录" }}
            </span>
          </div>
        </div>

        <nav class="nav-links">
          <RouterLink
            v-for="item in navItems"
            :key="item.name"
            class="nav-link"
            :to="{ name: item.name }"
          >
            <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
            <span class="nav-label">{{ item.label }}</span>
          </RouterLink>
        </nav>

        <div class="nav-footer">
          <button class="theme-toggle-button" @click="toggleTheme">
            <el-icon v-if="theme === 'dark'"><Sunny /></el-icon>
            <el-icon v-else><Moon /></el-icon>
            <span>{{ theme === 'dark' ? '浅色模式' : '深色模式' }}</span>
          </button>
          <button class="logout-button" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </button>
        </div>
      </aside>

      <!-- Main Content Area -->
      <main class="app-main">
        <header class="page-header">
          <div class="header-content">
            <div class="header-text">
              <div class="eyebrow">{{ eyebrow }}</div>
              <h1 class="page-title">{{ title }}</h1>
              <p class="page-subtitle">{{ subtitle }}</p>
            </div>
            <div class="header-actions">
              <slot name="header-actions" />
            </div>
          </div>
        </header>

        <div class="page-content">
          <slot />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ChatDotRound, Collection, Download, SwitchButton, Sunny, Moon } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { RouterLink, useRouter } from "vue-router";

import { useAuthStore } from "../stores/auth";
import { useTheme } from "../composables/useTheme";
import { notifyError } from "../utils/error";

const { theme, toggleTheme } = useTheme();

defineProps({
  eyebrow: {
    type: String,
    default: "Workspace",
  },
  title: {
    type: String,
    required: true,
  },
  subtitle: {
    type: String,
    required: true,
  },
});

const router = useRouter();
const authStore = useAuthStore();

const navItems = [
  { name: "import", label: "导入文章", icon: Download },
  { name: "articles", label: "文章列表", icon: Collection },
  { name: "sessions", label: "会话列表", icon: ChatDotRound },
];

async function handleLogout() {
  try {
    await ElMessageBox.confirm("确定要退出登录吗？", "退出确认", {
      confirmButtonText: "退出",
      cancelButtonText: "取消",
      type: "warning",
    });
  } catch {
    return;
  }
  try {
    await authStore.logout();
    ElMessage.success("已退出登录");
    await router.push({ name: "login" });
  } catch (error) {
    notifyError(error);
  }
}
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  background-color: var(--rb-bg);
}

/* Layout */
.app-layout {
  display: grid;
  grid-template-columns: var(--rb-sidebar) minmax(0, 1fr);
  min-height: 100vh;
}

/* Sidebar */
.app-sidebar {
  display: flex;
  flex-direction: column;
  height: 100vh;
  position: sticky;
  top: 0;
  padding: 32px 24px;
  background-color: var(--rb-panel);
  border-right: 1px solid var(--rb-border);
  z-index: 20;
}

/* Sidebar Branding */
.nav-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 40px;
  transition: opacity 0.2s ease;
  padding: 0 8px;
}

.nav-brand:hover {
  opacity: 0.8;
}

.nav-mark {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--rb-accent);
  border-radius: var(--rb-radius-md);
  font-family: var(--font-heading);
  font-weight: 700;
  font-size: 1.25rem;
  color: #fff;
  box-shadow: 0 0 20px rgba(var(--rb-accent-rgb), 0.2);
}

.nav-brand-text {
  display: flex;
  flex-direction: column;
}

.nav-brand-text strong {
  font-family: var(--font-heading);
  font-size: 1.15rem;
  font-weight: 700;
  letter-spacing: -0.01em;
  color: var(--rb-text);
  line-height: 1.2;
}

.nav-brand-subtitle {
  font-size: 0.75rem;
  color: var(--rb-text-soft);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* User Card */
.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  margin-bottom: 40px;
  background: var(--rb-bg-strong);
  border: 1px solid var(--rb-border);
  border-radius: var(--rb-radius-lg);
}

.user-avatar {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--rb-accent-soft);
  color: var(--rb-accent);
  border-radius: 50%;
  font-weight: 700;
  font-size: 1rem;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 0.875rem;
  font-weight: 600;
}

.user-status {
  font-size: 0.75rem;
  color: var(--rb-success);
}

/* Sidebar Action */
.sidebar-action {
  margin-bottom: 24px;
}

.btn-block {
  width: 100%;
  padding: 14px;
  border-radius: var(--rb-radius-md);
  font-weight: 700;
  gap: 12px;
  background: var(--rb-accent);
  color: #fff;
  text-decoration: none;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(var(--rb-accent-rgb), 0.2);
  transition: all 0.2s;
}

.btn-block:hover {
  background: var(--rb-accent-strong);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(var(--rb-accent-rgb), 0.3);
}

/* Navigation */
.nav-links {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: var(--rb-radius-md);
  color: var(--rb-text-soft);
  font-weight: 500;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 0.95rem;
  margin-bottom: 2px;
}

.nav-link:hover {
  background: var(--rb-bg-strong);
  color: var(--rb-text);
  transform: translateX(4px);
}

.nav-link.router-link-active {
  background: var(--rb-accent);
  color: #ffffff;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(var(--rb-accent-rgb), 0.25);
}

.nav-icon {
  font-size: 1.2rem;
}

/* Footer Actions */
.nav-footer {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-top: 24px;
  border-top: 1px solid var(--rb-border);
}

.theme-toggle-button, .logout-button {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  width: 100%;
  padding: 10px 14px;
  border-radius: var(--rb-radius-md);
  background: transparent;
  border: 1px solid transparent;
  color: var(--rb-text-soft);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.9rem;
}

.theme-toggle-button:hover, .logout-button:hover {
  background: var(--el-fill-color-light);
  color: var(--rb-text);
}

.logout-button:hover {
  color: var(--rb-danger);
  background: rgba(239, 68, 68, 0.1);
}

/* Main Content Area */
.app-main {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--rb-bg);
  overflow-x: hidden;
}

.page-header {
  padding: 32px 40px;
  background-color: var(--rb-panel);
  border-bottom: 1px solid var(--rb-border);
  backdrop-filter: blur(8px);
  position: relative;
  z-index: 10;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.header-text {
  flex: 1;
}

.header-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  min-width: 0;
}

.page-content {
  flex: 1;
  padding: 40px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .app-layout {
    grid-template-columns: 240px 1fr;
  }
}

@media (max-width: 768px) {
  .app-layout {
    grid-template-columns: 1fr;
  }

  .app-sidebar {
    height: auto;
    position: relative;
    border-right: none;
    border-bottom: 1px solid var(--rb-border);
  }

  .nav-links {
    flex-direction: row;
    overflow-x: auto;
    padding-bottom: 8px;
  }

  .nav-link {
    white-space: nowrap;
  }

  .header-content {
    flex-direction: column;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
