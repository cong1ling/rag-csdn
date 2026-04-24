<template>
  <el-dialog
    :model-value="modelValue"
    title="新建会话"
    width="560px"
    @close="emit('update:modelValue', false)"
  >
    <el-form ref="formRef" :model="form" label-position="top">
      <el-form-item label="会话类型" prop="sessionType">
        <el-radio-group v-model="form.sessionType" class="session-type-group">
          <el-radio-button v-for="item in SESSION_TYPE_OPTIONS" :key="item.value" :label="item.value">
            {{ item.label }}
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item
        v-if="form.sessionType === 'SINGLE_ARTICLE'"
        label="目标文章"
        prop="articleId"
        :error="articleError"
      >
        <el-select v-model="form.articleId" placeholder="请选择一篇已导入文章" class="full-width">
          <el-option v-for="article in articles" :key="article.id" :label="article.title" :value="article.id">
            <span>{{ article.title }}</span>
            <span class="muted code-text">{{ article.sourceId || article.bvid }}</span>
          </el-option>
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="toolbar">
        <el-button @click="emit('update:modelValue', false)">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">创建并进入对话</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from "vue";

import { SESSION_TYPE_OPTIONS } from "../constants/options";

const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  articles: {
    type: Array,
    default: () => [],
  },
  presetArticleId: {
    type: Number,
    default: null,
  },
  presetSessionType: {
    type: String,
    default: null,
  },
});

const emit = defineEmits(["update:modelValue", "submit"]);

const formRef = ref();
const form = reactive({
  sessionType: "SINGLE_ARTICLE",
  articleId: null,
});

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      form.sessionType = props.presetArticleId ? "SINGLE_ARTICLE" : props.presetSessionType || "ALL_ARTICLES";
      form.articleId = props.presetArticleId || null;
    }
  },
  { immediate: true }
);

const articleError = computed(() =>
  form.sessionType === "SINGLE_ARTICLE" && !form.articleId ? "单文章会话必须选择一篇文章" : ""
);

function handleSubmit() {
  if (articleError.value) {
    return;
  }
  emit("submit", {
    sessionType: form.sessionType,
    articleId: form.sessionType === "SINGLE_ARTICLE" ? form.articleId : null,
  });
}
</script>

<style scoped>
.session-type-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.session-type-group :deep(.el-radio-button__inner) {
  border-radius: var(--rb-radius-md);
}
</style>
