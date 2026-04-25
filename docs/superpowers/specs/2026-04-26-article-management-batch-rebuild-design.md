# Article Management Batch Rebuild Design

## Goal

Improve the article management page so users can:

1. Filter imported articles by import result status.
2. Select multiple articles from the current card list.
3. Submit batch rebuild actions efficiently without losing the existing card-based browsing experience.

This change is limited to article management UX and reuse of the existing single-article rebuild backend capability.

## Scope

### Included

- Add client-side status categories on the article management page:
  - `全部`
  - `导入成功`
  - `导入失败`
- Add card-level checkbox selection.
- Add a batch action bar for selected articles.
- Add batch rebuild flow that reuses the existing rebuild API per article.
- Prevent `IMPORTING` articles from being selected for batch rebuild.
- Keep the current article cards, detail drawer, delete action, and single rebuild action.

### Excluded

- No new backend batch rebuild API in this iteration.
- No migration from card layout to table layout.
- No pagination redesign.
- No server-side filtering.

## UX Design

### Page Structure

Keep the current overall page structure and card layout.

Add three interaction layers above the article grid:

1. Search input and refresh remain in the header actions.
2. A visible status filter strip is added near the article library header.
3. A batch action bar appears only when at least one article is selected.

### Status Filters

The filter strip exposes:

- `全部`
- `导入成功`
- `导入失败`

Behavior:

- Filtering is client-side over the already loaded article list.
- Filter labels show counts for convenience.
- Switching filters updates the visible card set immediately.
- Existing summary cards remain as high-level totals.

### Card Selection

Each article card gets a checkbox in its header area.

Behavior:

- `SUCCESS` and `FAILED` articles are selectable.
- `IMPORTING` articles show disabled selection state.
- Clicking the checkbox should not conflict with other card actions.
- Selection persists when switching filters, so users can choose across categories if needed.

### Batch Action Bar

Visible only when one or more selectable articles are chosen.

Contents:

- Selected count
- `全选当前筛选结果`
- `清空选择`
- `批量重建`

Convenience goals:

- Actions stay close to the content grid.
- The bar should be visually distinct but not heavy.
- On mobile, actions stack cleanly without causing horizontal overflow.

### Batch Rebuild Flow

When users click `批量重建`:

1. Show a confirmation dialog.
2. List the number of selected articles.
3. Clarify that the system will submit rebuild tasks one by one using the current article source URL.
4. After confirmation, call the existing rebuild API sequentially for the selected IDs.
5. Show a summary toast after completion:
   - submitted count
   - failed count
6. Refresh the article list after batch submission.

Sequential submission is preferred for this iteration because:

- It reuses the current backend behavior safely.
- It avoids creating a second burst of concurrent rebuild requests.
- It keeps UI failure reporting simple.

## Technical Design

### Frontend

Target file:

- `rag-csdn-front/src/views/ArticlesView.vue`

Add local state for:

- active status filter
- selected article IDs
- batch rebuild submitting state

Add computed values for:

- filtered article list by keyword + status
- selectable visible articles
- selected visible count

Add methods for:

- toggling one card selection
- selecting all currently filtered selectable cards
- clearing selection
- submitting batch rebuild sequentially

Update card rendering so checkbox state and disabled state are explicit.

### Backend

No new backend endpoint is required in this iteration.

Reuse:

- `POST /api/articles/{id}/rebuild`

The frontend loops through selected IDs and submits rebuild requests individually.

## Error Handling

- If one rebuild submission fails, continue submitting the remaining selected articles.
- Aggregate failures and show a final summary.
- Keep existing per-article failure reason display untouched.
- If list refresh fails after submission, show the refresh error separately.

## Testing

### Frontend verification

- Status filters switch correctly between all/success/failed.
- `IMPORTING` cards cannot be selected.
- Select all only targets visible selectable cards under the current filter.
- Batch rebuild submits selected articles sequentially.
- Partial failure still completes the remaining submissions.
- Layout stays usable on desktop and mobile.

### Regression focus

- Existing single rebuild dialog still works.
- Existing delete, detail, and create-session actions still work.
- Search continues to compose correctly with status filters.

## Risks

- If the article list grows large, client-side selection across filters may become harder to reason about. This is acceptable for the current scope.
- Sequential batch rebuild is slower than a dedicated backend batch task, but safer for now.

## Recommendation

Implement the batch rebuild and classification feature entirely in the frontend first, reusing the existing rebuild API and preserving the current card experience.
