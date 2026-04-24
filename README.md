[![Java CI with Maven](https://github.com/zshs000/rag-bilibili/actions/workflows/maven.yml/badge.svg)](https://github.com/zshs000/rag-bilibili/actions/workflows/maven.yml) [![Node.js CI](https://github.com/zshs000/rag-bilibili/actions/workflows/node.js.yml/badge.svg)](https://github.com/zshs000/rag-bilibili/actions/workflows/node.js.yml)

# rag-bilibili

仓库目录名仍保留 `rag-bilibili`，但当前前后端业务都已切换为基于 CSDN 社区文章的 RAG 系统。

## 当前状态

- 后端主包名已改为 `com.example.ragcsdn`
- 启动类已改为 `com.example.ragcsdn.RagCsdnApplication`
- 后端主业务接口已改为 `/api/articles`
- 前端主业务页面已改为 `/articles`
- `/api/videos` 目前保留为后端兼容别名，便于分阶段迁移
- 数据库与持久层已切换到文章语义：默认库名 `rag_csdn`，主表为 `article`，关联字段统一为 `article_id` / `source_id`

## 仓库结构

- `rag-csdn-server`：Spring Boot 后端，负责 CSDN 文章导入、向量检索、会话管理与 SSE 问答
- `rag-csdn-front`：Vue 3 前端，已切换到文章业务语义，当前主页面路由为 `/articles`

## 后端核心能力

- 导入 CSDN 文章链接并抽取正文内容
- 异步切分文章并写入 MySQL / DashVector
- 支持文章重建索引
- 支持基于会话范围的检索增强问答
- 支持 Query Rewrite、Hybrid Search、Rerank、动态 Top-K

## 后端关键路径

- 主包：`rag-csdn-server/src/main/java/com/example/ragcsdn`
- 主配置：[application.yml](./rag-csdn-server/src/main/resources/application.yml)
- 后端文档：[rag-csdn-server/README.md](./rag-csdn-server/README.md)
- 启动说明：[rag-csdn-server/后端启动配置文档.md](./rag-csdn-server/%E5%90%8E%E7%AB%AF%E5%90%AF%E5%8A%A8%E9%85%8D%E7%BD%AE%E6%96%87%E6%A1%A3.md)

## 当前后端配置约定

- Spring 应用名：`rag-csdn-server`
- DashVector Collection：`csdn`
- MyBatis type alias：`com.example.ragcsdn.entity`
- 主业务接口：`/api/articles`

说明：

- 当前数据库默认是 `rag_csdn`
- Flyway 已包含历史 `video/bvid/video_id` 结构迁移到 `article/source_id/article_id` 的前向脚本
- 新建库与增量升级都会落到当前 CSDN 文章业务结构

## 快速启动后端

```powershell
cd rag-csdn-server
mvn spring-boot:run
```

默认访问：

```text
http://localhost:8080
```

## 定向验证

本轮后端命名重构后，已通过以下定向测试：

- `ArticleControllerTest`
- `ArticleServiceImplTest`
- `ChatServiceImplTest`
- `SessionControllerTest`

执行命令：

```powershell
cd rag-csdn-server
mvn "-Dtest=ArticleControllerTest,ArticleServiceImplTest,ChatServiceImplTest,SessionControllerTest" test
```

## 说明

- 本轮已完成后端、前端与数据库层的文章业务命名同步。
- 如果后续确认前端已全部稳定运行，可以在下一步移除后端 `/api/videos` 兼容映射。

