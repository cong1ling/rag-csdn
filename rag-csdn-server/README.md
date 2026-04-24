# RAG-CSDN 后端服务

当前后端已从 Bilibili 字幕 RAG 切换为基于 CSDN 社区文章的 RAG 服务。

本轮后端改造已完成以下对外命名更新：

- 主包名：`com.example.ragcsdn`
- 启动类：`com.example.ragcsdn.RagCsdnApplication`
- 文章接口：主路由改为 `/api/articles`
- 兼容路由：保留 `/api/videos` 作为临时兼容别名，支持分阶段迁移

说明：

- 当前数据库默认库名为 `rag_csdn`。
- 持久层主表已切换为 `article`，关联字段统一为 `article_id` / `source_id`。
- Flyway 新增了历史 `video/bvid/video_id` 结构向当前文章结构的前向迁移脚本。
- 当前 DashVector 默认集合名已调整为 `csdn`。

## 核心能力

- CSDN 文章导入、切分、向量化
- MySQL 分片持久化与 DashVector 双写
- 基于会话范围的检索增强问答
- Query Rewrite、Hybrid Search、Rerank、动态 Top-K
- SSE 流式输出回答

## 项目结构

```text
src/main/java/com/example/ragcsdn/
├── RagCsdnApplication.java
├── controller/
│   ├── AuthController.java
│   ├── ArticleController.java
│   ├── SessionController.java
│   └── MessageController.java
├── service/
├── service/impl/
├── mapper/
├── entity/
├── dto/
├── config/
├── interceptor/
└── util/
```

## 关键接口

- `POST /api/articles`：导入文章
- `POST /api/articles/{id}/rebuild`：重建文章索引
- `GET /api/articles`：获取文章列表
- `GET /api/articles/{id}`：获取文章详情
- `DELETE /api/articles/{id}`：删除文章

兼容说明：

- 以上接口当前仍兼容 `/api/videos` 路径。

## 配置重点

参考文件：

- `src/main/resources/application.yml`
- `src/main/resources/application.yml.example`

当前关键配置：

```yaml
spring:
  application:
    name: rag-csdn-server
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/rag_csdn?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
  ai:
    alibaba:
      dashvector:
        collection: csdn

mybatis:
  type-aliases-package: com.example.ragcsdn.entity
```

## 启动方式

```powershell
cd rag-csdn-server
mvn spring-boot:run
```

或：

```powershell
mvn clean package
java -jar target/rag-csdn-server-1.0.0.jar
```

## 测试

```powershell
mvn test
```

当前这轮后端命名重构已验证通过的定向测试包括：

- `ArticleControllerTest`
- `ArticleServiceImplTest`
- `ChatServiceImplTest`
- `SessionControllerTest`

## 备注

- 联调页 `src/main/resources/static/dev.html`、建库脚本 `src/main/resources/schema.sql` 与 Flyway 迁移链都已同步到文章语义。
- 如果确认所有调用方都已切到 `/api/articles`，可以移除 `/api/videos` 兼容映射。

