[![Java CI with Maven](https://github.com/cong1ling/rag-csdn/actions/workflows/maven.yml/badge.svg)](https://github.com/cong1ling/rag-csdn/actions/workflows/maven.yml) [![Node.js CI](https://github.com/cong1ling/rag-csdn/actions/workflows/node.js.yml/badge.svg)](https://github.com/cong1ling/rag-csdn/actions/workflows/node.js.yml)

# RAG-CSDN

项目当前已经完成业务迁移，现阶段是一个面向 **CSDN 社区文章** 的 RAG 知识库系统。

## 项目介绍

项目围绕“导入文章 -> 切分与向量化 -> 检索增强问答 -> 会话沉淀”这条主链路构建。

当前系统支持用户导入 CSDN 文章链接，后端异步抓取正文、切分文本、写入 MySQL 与 DashVector，前端再基于文章或全库范围发起多轮对话，并以 SSE 方式实时返回回答内容与检索结果。

项目适合用于：

- 社区技术文章知识库沉淀
- 单篇文章精读问答
- 多篇文章横向检索与比对
- RAG 系统课程、实验或业务原型

## 使用技术栈及作用

| 分层 | 技术栈 | 作用 |
| --- | --- | --- |
| 前端 | Vue 3 + Vite | 构建单页应用与本地开发环境 |
| 前端 | Vue Router | 管理首页、登录、导入、文章列表、会话、聊天等路由 |
| 前端 | Pinia | 管理登录态、用户信息与页面状态 |
| 前端 | Element Plus | 提供表单、表格、对话框、消息提示等基础 UI 组件 |
| 前端 | Axios | 统一封装 HTTP 请求、错误处理与 JWT Header 注入 |
| 前端 | Marked + DOMPurify | 对聊天回答中的 Markdown 内容进行渲染与安全清洗 |
| 后端 | Spring Boot 3.2 | 提供 REST API、配置管理、依赖注入与应用启动能力 |
| 后端 | Spring AI 1.1.2 | 负责 ChatClient、向量检索抽象与 RAG 流程集成 |
| 后端 | DashScope Embedding | 负责文章分片向量化，默认使用 `text-embedding-v4` |
| 后端 | DashVector | 存储向量索引，承担相似度召回 |
| 后端 | MySQL 8 + Flyway | 存储用户、文章、分片、会话、消息与迁移脚本 |
| 后端 | MyBatis | 实现业务表访问与 SQL 映射 |
| 后端 | JWT + LoginInterceptor | 处理登录鉴权与接口访问保护 |
| 后端 | Bucket4j | 对注册、登录接口进行限流保护 |
| 后端 | Jsoup + 自定义 CSDN Reader | 负责文章链接解析、正文抽取与元数据整理 |
| 交互 | SSE | 支持聊天回答流式返回，提升对话交互体验 |

## 项目亮点

- 支持 **CSDN 文章导入、失败回写、重建索引**，导入链路是异步执行的，不阻塞 HTTP 请求线程。
- 检索侧集成了 **Query Rewrite、Query Understanding、HyDE、问题拆解、Hybrid Search、Rerank、动态 Top-K** 等优化策略。
- 会话侧支持 **历史摘要压缩、检索置信度判断、来源片段标注**，更接近可落地的 RAG 问答体验。
- 后端聊天接口基于 **SSE 流式输出**，前端可以实时展示回答增量内容。
- 数据层同时维护 **业务记录 + 分片记录 + 向量映射**，便于重建、删除与排障。
- 迁移过程中保留了 **`/api/videos` 与 `/videos` 兼容别名**，降低旧调用方切换成本。

## 项目迁移历史

1. 初始阶段：项目最早围绕 Bilibili 视频 / 字幕内容构建，仓库历史上也使用过 `rag-bilibili` 命名。
2. 检索增强阶段：在原始 RAG 问答链路上，逐步加入了混合检索、重排、动态 Top-K、对话摘要等能力，提升召回质量和多轮会话体验。
3. 业务域迁移阶段：项目从 “B 站视频问答” 迁移为 “CSDN 文章知识库”，后端主包切换到 `com.example.ragcsdn`，主应用切换到 `RagCsdnApplication`，主接口切换到 `/api/articles`，前端主页面切换到 `/articles`。
4. 数据迁移阶段：Flyway 通过 `V4__add_video_source_url.sql` 和 `V5__rename_video_schema_to_article_domain.sql` 将历史 `video / bvid / video_id` 结构前向迁移到当前 `article / source_id / article_id` 结构，并同步把 `SINGLE_VIDEO / ALL_VIDEOS` 会话类型升级为 `SINGLE_ARTICLE / ALL_ARTICLES`。
5. 当前阶段：前后端代码、数据库表、默认配置和主要业务语义均已统一到 CSDN 文章领域。

## 仓库结构

- `rag-csdn-server`：Spring Boot 后端，负责鉴权、文章导入、分片持久化、向量检索、会话管理与 SSE 问答
- `rag-csdn-front`：Vue 3 前端，负责登录注册、文章管理、会话入口与聊天交互
- `deployment`：部署脚本、systemd 服务文件与 Nginx 配置
- `QUICKSTART.md`：本地快速启动说明

## 快速开始

- 本地启动：查看 [QUICKSTART.md](./QUICKSTART.md)
- 后端启动说明：查看 [rag-csdn-server/后端启动配置文档.md](./rag-csdn-server/%E5%90%8E%E7%AB%AF%E5%90%AF%E5%8A%A8%E9%85%8D%E7%BD%AE%E6%96%87%E6%A1%A3.md)
- 前端启动说明：查看 [rag-csdn-front/前端快速启动文档.md](./rag-csdn-front/%E5%89%8D%E7%AB%AF%E5%BF%AB%E9%80%9F%E5%90%AF%E5%8A%A8%E6%96%87%E6%A1%A3.md)

## 当前默认约定

- 后端应用名：`rag-csdn-server`
- 后端端口：`8080`
- 前端开发端口：`5173`
- 默认数据库：`rag_csdn`
- DashVector Collection：`csdn`
- 后端主接口：`/api/articles`
- 后端兼容接口：`/api/videos`
- 前端主业务页：`/articles`
- 前端兼容路由：`/videos`
- 鉴权方式：`Authorization: Bearer <token>`

## 定向验证

当前仓库中与主链路相关的定向测试包括：

- `ArticleControllerTest`
- `ArticleServiceImplTest`
- `ChatServiceImplTest`
- `SessionControllerTest`

执行方式：

```powershell
cd rag-csdn-server
mvn "-Dtest=ArticleControllerTest,ArticleServiceImplTest,ChatServiceImplTest,SessionControllerTest" test
```

