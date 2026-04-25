# Article Management Batch Rebuild Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add status-based classification, multi-select cards, and a convenient batch rebuild flow to the article management page while keeping the existing card UI.

**Architecture:** Reuse the existing `POST /api/articles/{id}/rebuild` endpoint and implement batch rebuild entirely in the frontend with sequential submission. Extend the current `ArticlesView.vue` state model to compose keyword search, status filtering, card selection, and a conditional batch action bar without changing the backend contract.

**Tech Stack:** Vue 3 `script setup`, Element Plus, existing `articlesApi`, existing card-based article management UI.

---

## File Map

- Modify: `rag-csdn-front/src/views/ArticlesView.vue`
  - Add status filter state, selection state, computed lists, batch rebuild flow, and layout updates.
- Reuse: `rag-csdn-front/src/api/articles.js`
  - No API contract change expected; existing `rebuild(id)` is reused.

### Task 1: Add Filter and Selection State

**Files:**
- Modify: `rag-csdn-front/src/views/ArticlesView.vue`

- [ ] **Step 1: Write the failing UI behavior expectation in code comments/checklist before implementation**

Add these inline implementation targets near the existing reactive state block:

```js
// UI expectations for this feature:
// 1. keyword search composes with status filtering
// 2. selected IDs persist across filter switches
// 3. IMPORTING items cannot be selected
// 4. batch action bar appears only when selected IDs exist
```

- [ ] **Step 2: Add minimal state for filter and selection**

Extend the `script setup` state with:

```js
const statusFilter = ref("ALL");
const selectedArticleIds = ref([]);
const batchRebuildSubmitting = ref(false);
```

- [ ] **Step 3: Add computed helpers for filtered and selectable cards**

Implement:

```js
const statusFilteredVideos = computed(() => {
  if (statusFilter.value === "SUCCESS") {
    return videos.value.filter((item) => item.status === "SUCCESS");
  }
  if (statusFilter.value === "FAILED") {
    return videos.value.filter((item) => item.status === "FAILED");
  }
  return videos.value;
});

const filteredVideos = computed(() => {
  const query = keyword.value.toLowerCase();
  return statusFilteredVideos.value.filter(
    (video) =>
      !query ||
      video.title?.toLowerCase().includes(query) ||
      (video.sourceId || video.bvid || "").toLowerCase().includes(query)
  );
});

const selectableFilteredVideos = computed(() =>
  filteredVideos.value.filter((item) => item.status !== "IMPORTING")
);

const selectedCount = computed(() => selectedArticleIds.value.length);
```

- [ ] **Step 4: Add selection helper methods**

Implement:

```js
function isSelected(videoId) {
  return selectedArticleIds.value.includes(videoId);
}

function toggleSelection(video) {
  if (video.status === "IMPORTING") {
    return;
  }
  if (isSelected(video.id)) {
    selectedArticleIds.value = selectedArticleIds.value.filter((id) => id !== video.id);
    return;
  }
  selectedArticleIds.value = [...selectedArticleIds.value, video.id];
}

function clearSelection() {
  selectedArticleIds.value = [];
}

function selectAllFiltered() {
  const visibleIds = selectableFilteredVideos.value.map((item) => item.id);
  const merged = new Set([...selectedArticleIds.value, ...visibleIds]);
  selectedArticleIds.value = Array.from(merged);
}
```

- [ ] **Step 5: Run a build-oriented syntax check**

Run: `npm run build`
Expected: build may still fail because template/CSS updates are not finished, but no syntax error from the new script block.

- [ ] **Step 6: Commit**

```bash
git add rag-csdn-front/src/views/ArticlesView.vue
git commit -m "feat: add article filter and selection state"
```

### Task 2: Add Status Filter Strip and Batch Action Bar

**Files:**
- Modify: `rag-csdn-front/src/views/ArticlesView.vue`

- [ ] **Step 1: Update the article library header markup**

Add a filter strip and conditional batch action bar under the header:

```vue
<div class="library-toolbar">
  <div class="filter-strip">
    <el-button :type="statusFilter === 'ALL' ? 'primary' : 'default'" plain @click="statusFilter = 'ALL'">
      全部 {{ videos.length }}
    </el-button>
    <el-button :type="statusFilter === 'SUCCESS' ? 'success' : 'default'" plain @click="statusFilter = 'SUCCESS'">
      导入成功 {{ readyCount }}
    </el-button>
    <el-button :type="statusFilter === 'FAILED' ? 'danger' : 'default'" plain @click="statusFilter = 'FAILED'">
      导入失败 {{ failedCount }}
    </el-button>
  </div>

  <transition name="el-fade-in-linear">
    <div v-if="selectedCount" class="batch-toolbar surface-strong">
      <div class="batch-toolbar-copy">
        <strong>已选 {{ selectedCount }} 篇文章</strong>
        <p>可对当前已选文章批量提交重建任务。</p>
      </div>
      <div class="toolbar">
        <el-button plain @click="selectAllFiltered">全选当前筛选结果</el-button>
        <el-button plain @click="clearSelection">清空选择</el-button>
        <el-button type="primary" :loading="batchRebuildSubmitting" @click="submitBatchRebuild">批量重建</el-button>
      </div>
    </div>
  </transition>
</div>
```

- [ ] **Step 2: Add checkbox UI to each card**

Update the card header:

```vue
<div class="bento-item-header">
  <div class="card-select-group">
    <el-checkbox
      :model-value="isSelected(video.id)"
      :disabled="video.status === 'IMPORTING'"
      @change="toggleSelection(video)"
    />
    <span class="badge-inline code-text">{{ video.sourceId || video.bvid }}</span>
  </div>
  <StatusPill :label="statusMeta(video.status).label" :tone="statusMeta(video.status).tone" />
</div>
```

- [ ] **Step 3: Add layout styles for usability**

Add focused styles:

```css
.library-toolbar {
  display: grid;
  gap: 16px;
  margin-top: 20px;
}

.filter-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.batch-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 16px 18px;
  border: 1px solid var(--rb-border);
  border-radius: var(--rb-radius-md);
}

.card-select-group {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
```

- [ ] **Step 4: Add responsive behavior**

Extend the mobile media block:

```css
@media (max-width: 768px) {
  .batch-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-strip {
    width: 100%;
  }
}
```

- [ ] **Step 5: Run build to verify the UI compiles**

Run: `npm run build`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add rag-csdn-front/src/views/ArticlesView.vue
git commit -m "feat: add article status filters and batch action bar"
```

### Task 3: Implement Sequential Batch Rebuild Flow

**Files:**
- Modify: `rag-csdn-front/src/views/ArticlesView.vue`

- [ ] **Step 1: Add selected article lookup helper**

Implement:

```js
const selectedVideos = computed(() =>
  videos.value.filter((item) => selectedArticleIds.value.includes(item.id))
);
```

- [ ] **Step 2: Add batch rebuild submission method**

Implement sequential submission:

```js
async function submitBatchRebuild() {
  if (!selectedVideos.value.length) {
    ElMessage.warning("请先选择需要重建的文章");
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定批量重建 ${selectedVideos.value.length} 篇文章吗？系统会逐个提交重建任务。`,
      "批量重建",
      {
        confirmButtonText: "确认提交",
        cancelButtonText: "取消",
        type: "warning",
      }
    );
  } catch (error) {
    if (error === "cancel") {
      return;
    }
    throw error;
  }

  batchRebuildSubmitting.value = true;
  let submittedCount = 0;
  let failedCount = 0;

  try {
    for (const video of selectedVideos.value) {
      try {
        await articlesApi.rebuild(video.id);
        submittedCount += 1;
      } catch (error) {
        failedCount += 1;
      }
    }

    if (failedCount) {
      ElMessage.warning(`批量重建已提交 ${submittedCount} 篇，失败 ${failedCount} 篇`);
    } else {
      ElMessage.success(`批量重建已提交 ${submittedCount} 篇`);
    }

    clearSelection();
    await loadVideos();
  } catch (error) {
    notifyError(error);
  } finally {
    batchRebuildSubmitting.value = false;
  }
}
```

- [ ] **Step 3: Keep single rebuild flow intact**

Verify `openRebuildDialog`, `closeRebuildDialog`, and `submitRebuild` remain unchanged except for any necessary selection cleanup after list refresh.

- [ ] **Step 4: Run build to verify passes**

Run: `npm run build`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add rag-csdn-front/src/views/ArticlesView.vue
git commit -m "feat: add sequential batch rebuild for articles"
```

### Task 4: Regression Verification

**Files:**
- Modify: `rag-csdn-front/src/views/ArticlesView.vue` if any final fixes are needed

- [ ] **Step 1: Verify search + status filter composition**

Manual check targets:

```text
1. 搜索词为空时，全部/成功/失败分类切换正常
2. 搜索词非空时，分类仍按搜索结果继续过滤
3. IMPORTING 卡片复选框为禁用状态
```

- [ ] **Step 2: Verify batch selection usability**

Manual check targets:

```text
1. 选择单张卡片后，批量操作条出现
2. 全选当前筛选结果只选中当前可见且非 IMPORTING 的卡片
3. 清空选择后，批量操作条消失
```

- [ ] **Step 3: Verify existing actions still behave**

Manual check targets:

```text
1. 单篇重建弹窗仍可打开并提交
2. 进入问答按钮仍只对 SUCCESS 可用
3. 删除与详情功能仍可使用
```

- [ ] **Step 4: Run final build**

Run: `npm run build`
Expected: PASS with generated production bundle

- [ ] **Step 5: Commit**

```bash
git add rag-csdn-front/src/views/ArticlesView.vue
git commit -m "chore: verify article management batch rebuild UI"
```

## Self-Review

- Spec coverage: status filters, multi-select, batch action bar, sequential rebuild, `IMPORTING` disabled selection, and mobile-friendly layout are all covered by Tasks 1-4.
- Placeholder scan: no `TODO`/`TBD` placeholders remain.
- Type consistency: plan consistently uses `statusFilter`, `selectedArticleIds`, `batchRebuildSubmitting`, `filteredVideos`, `selectedVideos`, and `submitBatchRebuild`.
