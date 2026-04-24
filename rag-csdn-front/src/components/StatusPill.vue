<template>
  <span class="status-pill" :class="tone">
    <span class="status-dot"></span>
    <span class="status-label">{{ label }}</span>
  </span>
</template>

<script setup>
defineProps({
  label: {
    type: String,
    required: true,
  },
  tone: {
    type: String,
    default: "success",
  },
});
</script>

<style scoped>
.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 8px;
  border-radius: var(--rb-radius-sm);
  font-size: 0.75rem;
  font-weight: 500;
  line-height: 1.5;
  background: var(--rb-bg);
  border: 1px solid var(--rb-border);
  color: var(--rb-text-muted);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--rb-text-muted);
}

/* Success */
.status-pill.success {
  background: rgba(16, 185, 129, 0.05);
  border-color: rgba(16, 185, 129, 0.2);
  color: #10b981;
}
.status-pill.success .status-dot {
  background-color: #10b981;
}

/* Warning */
.status-pill.warning {
  background: rgba(245, 158, 11, 0.05);
  border-color: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
}
.status-pill.warning .status-dot {
  background-color: #f59e0b;
}

/* Error */
.status-pill.error {
  background: rgba(239, 68, 68, 0.05);
  border-color: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}
.status-pill.error .status-dot {
  background-color: #ef4444;
}

/* Info */
.status-pill.info {
  background: rgba(59, 130, 246, 0.05);
  border-color: rgba(59, 130, 246, 0.2);
  color: #3b82f6;
}
.status-pill.info .status-dot {
  background-color: #3b82f6;
}

/* Neutral/Neon override to info in Enterprise mode */
.status-pill.neon {
  background: rgba(59, 130, 246, 0.05);
  border-color: rgba(59, 130, 246, 0.2);
  color: #3b82f6;
}
.status-pill.neon .status-dot {
  background-color: #3b82f6;
}
</style>
