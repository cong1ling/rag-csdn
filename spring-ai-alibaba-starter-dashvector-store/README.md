# DashVector Vector Store

[English](#english) | [中文](#中文)

## English

DashVector is a fully managed vector database service provided by Alibaba Cloud, designed for AI applications requiring high-performance vector similarity search. This module provides Spring AI integration for DashVector.

### Features

- **High Performance**: Optimized for large-scale vector similarity search
- **Fully Managed**: No infrastructure management required
- **Metadata Filtering**: Supports translating Spring AI `filterExpression` to DashVector SQL-like metadata filters
- **Spring Boot Auto-Configuration**: Seamless integration with Spring Boot applications
- **Observability**: Built-in support for metrics and tracing

### Configuration

Add the following configuration to your `application.yml`:

```yaml
spring:
  ai:
    vectorstore:
      dashvector:
        enabled: true
        api-key: ${DASHVECTOR_API_KEY}
        endpoint: ${DASHVECTOR_ENDPOINT}
        collection-name: my_collection
        dimension: 1536  # Optional, inferred from EmbeddingModel if not set
        metric: cosine   # cosine, euclidean, or dotproduct
        default-top-k: 10
        default-similarity-threshold: 0.0
        initialize-schema: false
        timeout: 30.0
```

### Usage Example

```java
@Autowired
private VectorStore vectorStore;

public void example() {
    // Add documents
    List<Document> documents = List.of(
        new Document("Spring AI is awesome", Map.of("category", "tech")),
        new Document("DashVector is fast", Map.of("category", "database"))
    );
    vectorStore.add(documents);

    // Search with filter
    SearchRequest request = SearchRequest.builder()
        .query("AI framework")
        .topK(5)
        .similarityThreshold(0.7)
        .filterExpression("category == 'tech' && year >= 2024")
        .build();
    List<Document> results = vectorStore.similaritySearch(request);
}
```

### Recommended Filter Usage

Prefer simple scalar metadata fields, for example:

```java
SearchRequest request = SearchRequest.builder()
    .query("AI framework")
    .topK(5)
    .similarityThreshold(0.7)
    .filterExpression("category == 'tech' && year >= 2024")
    .build();

List<Document> results = vectorStore.similaritySearch(request);
```

Recommended metadata shape:

- `category`: string
- `year`: number
- `published`: boolean

Avoid relying on complex nested structures or cross-store identical filter semantics.

### Limitations

- **Filter-based Deletion**: DashVector does not support deletion by filter expression. Use `delete(List<String> ids)` instead.
- **Collection Management**: Collections must be created externally before using this store. Set `initialize-schema: false` (default).
- **Cosine Score Conversion**: When `metric: cosine` is used, DashVector returns cosine distance. This store converts it to Spring AI similarity with `1 - distance` before applying `similarityThreshold` and populating `Document.score`.
- **Filter Semantics**: Query filtering is currently supported, but it is translated to DashVector field filters over flattened metadata fields. This is a minimal compatibility layer, not a guarantee of identical cross-store semantics for every Spring AI filter expression.
- **Filter Scope**: Prefer simple scalar metadata fields and basic comparison logic. Complex or store-specific filter semantics should not be assumed portable.

### Dependencies

```xml
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-starter-store-dashvector</artifactId>
</dependency>
```

---

## 中文

DashVector 是阿里云提供的全托管向量数据库服务，专为需要高性能向量相似性搜索的 AI 应用而设计。本模块为 DashVector 提供 Spring AI 集成。

### 主要特性

- **高性能**：针对大规模向量相似性搜索进行优化
- **全托管**：无需管理基础设施
- **元数据过滤**：支持将 Spring AI 的 `filterExpression` 转换为 DashVector 的类 SQL 元数据过滤条件
- **Spring Boot 自动配置**：与 Spring Boot 应用无缝集成
- **可观测性**：内置指标和追踪支持

### 配置

在 `application.yml` 中添加以下配置：

```yaml
spring:
  ai:
    vectorstore:
      dashvector:
        enabled: true
        api-key: ${DASHVECTOR_API_KEY}
        endpoint: ${DASHVECTOR_ENDPOINT}
        collection-name: my_collection
        dimension: 1536  # 可选，如果不设置则从 EmbeddingModel 推断
        metric: cosine   # cosine、euclidean 或 dotproduct
        default-top-k: 10
        default-similarity-threshold: 0.0
        initialize-schema: false
        timeout: 30.0
```

### 使用示例

```java
@Autowired
private VectorStore vectorStore;

public void example() {
    // 添加文档
    List<Document> documents = List.of(
        new Document("Spring AI is awesome", Map.of("category", "tech")),
        new Document("DashVector is fast", Map.of("category", "database"))
    );
    vectorStore.add(documents);

    // 带过滤条件的搜索
    SearchRequest request = SearchRequest.builder()
        .query("AI framework")
        .topK(5)
        .similarityThreshold(0.7)
        .filterExpression("category == 'tech' && year >= 2024")
        .build();
    List<Document> results = vectorStore.similaritySearch(request);
}
```

### 推荐的过滤用法

建议优先使用简单的标量 metadata 字段，例如：

```java
SearchRequest request = SearchRequest.builder()
    .query("AI framework")
    .topK(5)
    .similarityThreshold(0.7)
    .filterExpression("category == 'tech' && year >= 2024")
    .build();

List<Document> results = vectorStore.similaritySearch(request);
```

推荐的 metadata 形态：

- `category`：字符串
- `year`：数值
- `published`：布尔值

不建议依赖复杂嵌套结构，或假设其过滤语义与其他 VectorStore 完全一致。

### 限制

- **基于过滤器的删除**：DashVector 不支持按过滤表达式删除。请使用 `delete(List<String> ids)` 代替。
- **集合管理**：在使用此存储之前，必须在外部创建集合。设置 `initialize-schema: false`（默认值）。
- **余弦分数转换**：当 `metric: cosine` 时，DashVector 返回的是余弦距离。本实现会先按 `1 - distance` 转成 Spring AI 相似度，再应用 `similarityThreshold` 并写入 `Document.score`。
- **过滤语义边界**：当前查询过滤是支持的，但实现方式是把 Spring AI 的过滤表达式转换为 DashVector 对扁平化 metadata 字段的过滤。这是最小兼容适配，不保证所有 Spring AI 过滤表达式在不同 VectorStore 间都具备完全一致的语义。
- **过滤使用建议**：优先使用简单标量字段和基础比较逻辑；复杂过滤或强依赖底层存储语义的场景，暂不建议按“跨库完全可移植”来使用。

### 依赖

```xml
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-starter-store-dashvector</artifactId>
</dependency>
```
