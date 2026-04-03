<template>
  <div class="empty-block surface-strong">
    <StatusPill :label="badge" tone="info" class="empty-badge" />
    <div class="empty-icon-wrapper">
      <div class="empty-icon">
        <slot name="icon">📁</slot>
      </div>
    </div>
    <h3 class="empty-title">{{ title }}</h3>
    <p class="empty-desc">{{ description }}</p>
    <div class="empty-actions">
      <slot />
    </div>
  </div>
</template>

<script setup>
import StatusPill from "./StatusPill.vue";

defineProps({
  badge: {
    type: String,
    default: "EMPTY",
  },
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    required: true,
  },
});
</script>

<style scoped>
.empty-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 48px 24px;
  border-radius: var(--rb-radius-md);
  border: 1px dashed var(--rb-border);
  background: var(--rb-panel-strong);
  min-height: 250px;
}

.empty-badge {
  margin-bottom: 16px;
}

.empty-icon-wrapper {
  margin-bottom: 16px;
}

.empty-icon {
  font-size: 2rem;
  color: var(--rb-text-muted);
}

.empty-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--rb-text);
  margin: 0 0 8px 0;
}

.empty-desc {
  color: var(--rb-text-muted);
  font-size: 0.875rem;
  max-width: 400px;
  margin: 0 0 20px 0;
  line-height: 1.5;
}

.empty-actions {
  display: flex;
  gap: 12px;
}
</style>
