<template>
  <div class="markdown-content" :class="{ 'is-streaming': streaming }" v-html="renderedHtml"></div>
</template>

<script setup>
import { computed } from "vue";
import { marked } from "marked";
import DOMPurify from "dompurify";

const props = defineProps({
  content: {
    type: String,
    default: "",
  },
  streaming: {
    type: Boolean,
    default: false,
  }
});

// 配置marked
marked.setOptions({
  breaks: true,
  gfm: true,
});

const renderedHtml = computed(() => {
  if (!props.content) {
    return "";
  }
  try {
    const rawHtml = marked.parse(props.content);
    // 使用DOMPurify净化HTML，防止XSS攻击
    return DOMPurify.sanitize(rawHtml, {
      ALLOWED_TAGS: [
        'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
        'p', 'br', 'hr',
        'ul', 'ol', 'li',
        'a', 'strong', 'em', 'code', 'pre',
        'blockquote', 'table', 'thead', 'tbody', 'tr', 'th', 'td',
        'img', 'span', 'div'
      ],
      ALLOWED_ATTR: ['href', 'src', 'alt', 'title', 'class'],
      ALLOW_DATA_ATTR: false,
    });
  } catch (error) {
    console.error("Markdown解析失败:", error);
    return DOMPurify.sanitize(props.content);
  }
});
</script>

<style scoped>
.markdown-content {
  line-height: 1.6;
  word-wrap: break-word;
  color: var(--rb-text);
  font-size: 0.95rem;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  margin-top: 1.5em;
  margin-bottom: 0.5em;
  font-weight: 600;
  color: var(--rb-text);
  line-height: 1.3;
}

.markdown-content :deep(h1) {
  font-size: 1.5em;
  border-bottom: 1px solid var(--rb-border);
  padding-bottom: 0.3em;
}

.markdown-content :deep(h2) {
  font-size: 1.25em;
  border-bottom: 1px solid var(--rb-border);
  padding-bottom: 0.3em;
}

.markdown-content :deep(h3) {
  font-size: 1.1em;
}

.markdown-content :deep(p) {
  margin-bottom: 0.8em;
  margin-top: 0;
}

.markdown-content :deep(p:last-child) {
  margin-bottom: 0;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin-bottom: 1em;
  padding-left: 1.5em;
}

.markdown-content :deep(li) {
  margin-bottom: 0.25em;
}

.markdown-content :deep(code) {
  background-color: var(--rb-panel-strong);
  padding: 0.2em 0.4em;
  border-radius: 4px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 0.85em;
  color: var(--rb-text);
  border: 1px solid var(--rb-border);
}

.markdown-content :deep(pre) {
  background-color: var(--rb-panel-strong);
  border: 1px solid var(--rb-border);
  padding: 1em;
  border-radius: var(--rb-radius-md);
  overflow-x: auto;
  margin-bottom: 1em;
}

.markdown-content :deep(pre code) {
  background-color: transparent;
  padding: 0;
  border: none;
  color: var(--rb-text);
  font-size: 0.85rem;
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid var(--rb-border-hover);
  padding-left: 1em;
  margin-left: 0;
  margin-right: 0;
  color: var(--rb-text-muted);
  margin-bottom: 1em;
  background: var(--rb-panel);
  padding: 12px 16px;
  border-radius: 0 var(--rb-radius-md) var(--rb-radius-md) 0;
}

.markdown-content :deep(a) {
  color: #3b82f6;
  text-decoration: none;
}

.markdown-content :deep(a:hover) {
  text-decoration: underline;
}

.markdown-content :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin-bottom: 1em;
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  border: 1px solid var(--rb-border);
  padding: 0.5em 1em;
  text-align: left;
}

.markdown-content :deep(th) {
  background-color: var(--rb-panel);
  font-weight: 600;
}

.markdown-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: var(--rb-radius-md);
  border: 1px solid var(--rb-border);
}

.markdown-content :deep(hr) {
  border: none;
  border-top: 1px solid var(--rb-border);
  margin: 1.5em 0;
}

/* Streaming Animation */
.is-streaming::after {
  content: '▋';
  display: inline-block;
  vertical-align: text-bottom;
  animation: blink 1s step-end infinite;
  color: var(--rb-text);
  margin-left: 4px;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}
</style>
