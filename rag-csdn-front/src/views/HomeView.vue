<template>
  <div class="home-page">
    <SiteHeader />
    <Background3D />
    
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title heading-primary">
          将 CSDN 文章转化为<br />
          可智能检索的企业级知识库
        </h1>

        <p class="hero-description">
          采用企业级 RAG（检索增强生成）架构，自动化完成文章抓取与向量化嵌入。与社区内容进行深度对话交互，为学习、研究及业务分析打造全自动化的信息处理工作流。
        </p>

        <div class="hero-actions">
          <RouterLink :to="authStore.isAuthenticated ? '/articles' : '/login'" class="btn btn-primary btn-xl">
            <span>开始构建知识库</span>
            <el-icon><ArrowRight /></el-icon>
          </RouterLink>
          <a href="#features" class="btn btn-secondary btn-xl">探索核心能力</a>
        </div>

        <div class="hero-stats">
          <div class="stat-item">
            <div class="stat-number">1. 粘贴链接</div>
            <div class="stat-label">正文抓取与解析</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-number">2. 构建向量</div>
            <div class="stat-label">深层语义知识索引</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-number">3. 开启对话</div>
            <div class="stat-label">AI 驱动的内容洞察</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section id="features" class="features-section">
      <div class="section-header">
        <h2 class="section-title">核心能力架构</h2>
        <p class="section-description">
          依托检索增强生成（RAG）技术，打破传统文章内容难检索、难复用的瓶颈，持续释放社区知识的深层价值。
        </p>
      </div>

      <div class="features-grid">
        <RouterLink to="/import" class="feature-card surface">
          <div class="feature-icon">
            <el-icon><Download /></el-icon>
          </div>
          <h3 class="feature-title">无缝接入与自动化处理</h3>
          <p class="feature-description">
            支持输入任意 CSDN 文章链接，系统将在后台自动抓取正文、执行语义化文本分块，并安全存入向量数据库。
          </p>
          <div class="feature-action">
            <span>开始导入</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </RouterLink>

        <RouterLink to="/sessions" class="feature-card surface">
          <div class="feature-icon">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <h3 class="feature-title">实时生成式智能问答</h3>
          <p class="feature-description">
            基于文章内容发起多轮自然语言对话。智能引擎毫秒级精准检索相关片段，流式生成带有来源追溯的专业解答。
          </p>
          <div class="feature-action">
            <span>开启对话</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </RouterLink>

        <RouterLink to="/articles" class="feature-card surface">
          <div class="feature-icon">
            <el-icon><Collection /></el-icon>
          </div>
          <h3 class="feature-title">跨文章全局知识检索</h3>
          <p class="feature-description">
            打破单篇文章限制，支持在全量知识库中进行全局多维度检索，发现知识关联、提炼对比洞察，构建结构化中心库。
          </p>
          <div class="feature-action">
            <span>浏览库</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </RouterLink>
      </div>
    </section>

    <!-- How It Works Section -->
    <section id="how-it-works" class="how-it-works-section">
      <div class="section-header">
        <h2 class="section-title">标准化业务流程</h2>
      </div>

      <div class="steps-container">
        <div class="step-item">
          <div class="step-header">第一步</div>
          <h3 class="step-title">数据接入与清洗</h3>
          <p class="step-description">输入目标 CSDN 文章链接，系统自动抓取正文、执行文本清洗与最优长度的语义切分，为向量化做好准备。</p>
        </div>

        <div class="step-arrow">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>

        <div class="step-item">
          <div class="step-header">第二步</div>
          <h3 class="step-title">构建索引与检索域</h3>
          <p class="step-description">可自由勾选特定文章子集，或面向全局存储库构建高维索引，以此作为大模型知识生成的置信上下文。</p>
        </div>

        <div class="step-arrow">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>

        <div class="step-item">
          <div class="step-header">第三步</div>
          <h3 class="step-title">深度交互与知识提炼</h3>
          <p class="step-description">借助 AI 引擎，秒级完成长文摘要生成、核心要点提取以及复杂业务逻辑的提问与解答验证。</p>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section">
      <div class="cta-content surface">
        <h2 class="cta-title">准备好重塑您的知识沉淀工作流了吗？</h2>
        <p class="cta-description">
          立即启用面向非结构化社区文章数据的企业级智能检索解决方案。
        </p>
        <div class="cta-actions">
          <RouterLink :to="authStore.isAuthenticated ? '/articles' : '/register'" class="btn btn-primary btn-large">
            {{ authStore.isAuthenticated ? '进入工作台' : '注册账号' }}
          </RouterLink>
          <RouterLink v-if="!authStore.isAuthenticated" to="/login" class="btn btn-secondary btn-large">
            系统登录
          </RouterLink>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
        <div class="footer-content">
          <div class="footer-brand">
            <div class="nav-mark">RC</div>
            <div>
            <div class="logo-text">RAG CSDN</div>
            <p class="footer-tagline">企业级智能知识管理系统</p>
          </div>
        </div>

        <div class="footer-links">
          <div class="footer-column">
            <h4 class="footer-column-title">产品中心</h4>
            <RouterLink to="/import" class="footer-link">导入文章</RouterLink>
            <RouterLink to="/articles" class="footer-link">文章列表</RouterLink>
            <RouterLink to="/sessions" class="footer-link">会话列表</RouterLink>
          </div>

          <div class="footer-column">
            <h4 class="footer-column-title">技术底座</h4>
            <span class="footer-text">Spring AI</span>
            <span class="footer-text">Vue 3</span>
            <span class="footer-text">向量数据库 (Vector DB)</span>
          </div>
        </div>
      </div>

      <div class="footer-bottom">
        <p class="footer-copyright">© 2026 RAG CSDN. 保留所有权利。</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { Moon, Sunny, Download, Collection, ChatDotRound, ArrowRight } from "@element-plus/icons-vue";
import { RouterLink } from "vue-router";

import { useTheme } from "../composables/useTheme";
import { useAuthStore } from "../stores/auth";

import Background3D from "../components/Background3D.vue";
import SiteHeader from "../components/SiteHeader.vue";

const { theme, toggleTheme } = useTheme();
const authStore = useAuthStore();
</script>

<style scoped>
.home-page {
  position: relative;
  min-height: 100vh;
  color: var(--rb-text);
  overflow: hidden;
  isolation: isolate;
}

.home-page > * {
  position: relative;
  z-index: 1;
}

/* Base Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: var(--rb-radius-md);
  font-family: var(--font-body);
  font-size: 0.95rem;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s ease;
  cursor: pointer;
  border: 1px solid transparent;
}

.btn-primary {
  background: var(--rb-accent);
  color: #ffffff;
}

.btn-primary:hover {
  background: var(--rb-accent-strong);
}

.btn-secondary {
  background: transparent;
  border: 1px solid var(--rb-border);
  color: var(--rb-text);
}

.btn-secondary:hover {
  border-color: var(--rb-text-soft);
  background: var(--el-fill-color-light);
}

.btn-large {
  padding: 14px 32px;
  font-size: 1.05rem;
}

.btn-xl {
  padding: 16px 40px;
  font-size: 1.15rem;
  font-weight: 700;
  border-radius: var(--rb-radius-lg);
  box-shadow: 0 8px 24px rgba(var(--rb-accent-rgb), 0.3);
}

.btn-xl:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(var(--rb-accent-rgb), 0.4);
}

.theme-toggle-button {
  display: inline-flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
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

.theme-toggle-button:hover {
  background: var(--el-fill-color-light);
  color: var(--rb-text);
}

/* Hero Section */
.hero-section {
  position: relative;
  padding: 120px 24px 80px;
  display: flex;
  justify-content: center;
  text-align: center;
  border-bottom: 1px solid var(--rb-border);
  background: var(--rb-panel);
  overflow: hidden;
}

.home-theme-toggle {
  position: absolute;
  top: 24px;
  right: 24px;
  z-index: 2;
  backdrop-filter: blur(8px);
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 800px;
  width: 100%;
}

.hero-title {
  margin-bottom: 24px;
  color: var(--rb-text);
}

.hero-description {
  font-size: 1.15rem;
  line-height: 1.6;
  color: var(--rb-text-soft);
  margin: 0 auto 40px;
  max-width: 680px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 60px;
}

.hero-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 40px;
  padding: 24px 0;
}

.stat-item {
  text-align: left;
}

.stat-number {
  font-family: var(--font-heading);
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--rb-text);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 0.85rem;
  color: var(--rb-text-soft);
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: var(--rb-border);
}

/* Common Section Styles */
.features-section, .how-it-works-section, .cta-section {
  padding: 100px 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  text-align: center;
  margin-bottom: 64px;
}

.section-title {
  font-family: var(--font-heading);
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 16px;
  letter-spacing: -0.02em;
}

.section-description {
  font-size: 1.1rem;
  color: var(--rb-text-soft);
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
}

/* Features Grid */
.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.feature-card {
  padding: 32px;
  background: var(--rb-panel);
  display: flex;
  flex-direction: column;
  text-decoration: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid var(--rb-border);
  height: 100%;
}

.feature-card:hover {
  transform: translateY(-8px);
  border-color: var(--rb-accent);
  background: var(--rb-panel-strong);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

.feature-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--rb-radius-md);
  background: var(--rb-bg-strong);
  color: var(--rb-accent);
  margin-bottom: 24px;
  font-size: 1.5rem;
  border: 1px solid var(--rb-border);
}

.feature-card:hover .feature-icon {
  background: var(--rb-accent);
  color: #fff;
  border-color: var(--rb-accent);
}

.feature-title {
  font-family: var(--font-heading);
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--rb-text);
}

.feature-description {
  font-size: 1rem;
  line-height: 1.6;
  color: var(--rb-text-soft);
  margin-bottom: 24px;
  flex: 1;
}

.feature-action {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--rb-accent);
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.3s ease;
}

.feature-card:hover .feature-action {
  opacity: 1;
  transform: translateX(0);
}

/* Steps Container */
.steps-container {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
}

.step-item {
  flex: 1;
  text-align: left;
}

.step-header {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--rb-accent);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 12px;
}

.step-title {
  font-family: var(--font-heading);
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 12px;
}

.step-description {
  font-size: 1rem;
  color: var(--rb-text-soft);
  line-height: 1.6;
}

.step-arrow {
  color: var(--rb-border);
  display: flex;
  align-items: center;
  justify-content: center;
  padding-top: 40px;
}

/* CTA Section */
.cta-content {
  padding: 64px;
  text-align: center;
  background: var(--rb-panel);
}

.cta-title {
  font-family: var(--font-heading);
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 16px;
}

.cta-description {
  font-size: 1.1rem;
  color: var(--rb-text-soft);
  margin-bottom: 32px;
}

.cta-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

/* Footer */
.footer {
  padding: 64px 24px 32px;
  background: var(--rb-bg-strong);
  border-top: 1px solid var(--rb-border);
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto 48px;
  display: flex;
  justify-content: space-between;
  gap: 64px;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-mark {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--rb-accent);
  border-radius: var(--rb-radius-md);
  font-family: var(--font-heading);
  font-weight: 700;
  font-size: 1.1rem;
  color: #fff;
}

.logo-text {
  font-family: var(--font-heading);
  font-size: 1.1rem;
  font-weight: 600;
}

.footer-tagline {
  color: var(--rb-text-soft);
  font-size: 0.85rem;
  margin-top: 4px;
}

.footer-links {
  display: flex;
  gap: 64px;
}

.footer-column {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer-column-title {
  font-family: var(--font-heading);
  font-size: 0.95rem;
  font-weight: 600;
  margin-bottom: 4px;
}

.footer-link, .footer-text {
  color: var(--rb-text-soft);
  font-size: 0.9rem;
}

.footer-link:hover {
  color: var(--rb-text);
}

.footer-bottom {
  max-width: 1200px;
  margin: 0 auto;
  padding-top: 32px;
  border-top: 1px solid var(--rb-border);
  text-align: left;
}

.footer-copyright {
  color: var(--rb-text-soft);
  font-size: 0.85rem;
}

/* Responsive */
@media (max-width: 1024px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .home-page::before {
    background-size: 48px 48px;
  }

  .home-page::after {
    height: 320px;
  }

  .hero-section {
    padding: 80px 24px 60px;
  }

  .home-theme-toggle {
    top: 16px;
    right: 16px;
  }

  .geo-sphere {
    top: 18px;
    right: -44px;
    width: 168px;
    height: 168px;
  }

  .geo-cube {
    left: -6px;
    bottom: 40px;
    width: 92px;
    height: 92px;
    transform: perspective(480px) rotateX(8deg) rotateY(-16deg) rotate(10deg) translate3d(0, 0, 0);
  }

  .geo-pyramid {
    display: none;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .steps-container {
    flex-direction: column;
    gap: 32px;
  }

  .step-arrow {
    display: none;
  }

  .hero-stats {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .stat-divider {
    display: none;
  }

  .footer-content {
    flex-direction: column;
  }
}

@media (prefers-reduced-motion: reduce) {
  .geo,
  .geo-sphere,
  .geo-cube,
  .geo-pyramid {
    animation: none !important;
  }
}
</style>
