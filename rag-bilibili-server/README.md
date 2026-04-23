# RAG-Bilibili 后端服务

基于 B 站视频字幕内容的检索增强问答系统后端服务。

当前实现基于 Spring Boot + Spring AI + DashVector，已经具备完整的导入、检索、会话和 SSE 流式问答链路。

## 项目结构

```
rag-bilibili-server/
├── src/main/java/com/example/ragbilibili/
│   ├── RagBilibiliApplication.java          # 主应用类
│   ├── config/                               # 配置类
│   │   ├── SpringAIConfig.java              # Spring AI 配置
│   │   └── WebConfig.java                   # Web MVC 配置
│   ├── controller/                           # 控制器层
│   │   ├── AuthController.java              # 认证控制器
│   │   ├── VideoController.java             # 视频控制器
│   │   ├── SessionController.java           # 会话控制器
│   │   └── MessageController.java           # 消息控制器
│   ├── service/                              # 服务层接口
│   │   ├── UserService.java
│   │   ├── VideoService.java
│   │   ├── SessionService.java
│   │   ├── MessageService.java
│   │   └── ChatService.java
│   ├── service/impl/                         # 服务层实现
│   ├── mapper/                               # MyBatis Mapper 接口
│   │   ├── UserMapper.java
│   │   ├── VideoMapper.java
│   │   ├── ChunkMapper.java
│   │   ├── VectorMappingMapper.java
│   │   ├── SessionMapper.java
│   │   └── MessageMapper.java
│   ├── entity/                               # 实体类
│   │   ├── User.java
│   │   ├── Video.java
│   │   ├── Chunk.java
│   │   ├── VectorMapping.java
│   │   ├── Session.java
│   │   └── Message.java
│   ├── dto/                                  # 数据传输对象
│   │   ├── request/                          # 请求 DTO
│   │   └── response/                         # 响应 DTO
│   ├── enums/                                # 枚举类
│   │   ├── SessionType.java
│   │   ├── MessageRole.java
│   │   └── VideoStatus.java
│   ├── exception/                            # 异常处理
│   │   ├── BusinessException.java
│   │   ├── ErrorCode.java
│   │   └── GlobalExceptionHandler.java
│   ├── util/                                 # 工具类
│   │   ├── BVIDParser.java
│   │   ├── VectorIDGenerator.java
│   │   └── PasswordEncoder.java
│   ├── common/                               # 通用类
│   │   └── Result.java
│   └── interceptor/                          # 拦截器
│       └── LoginInterceptor.java
├── src/main/resources/
│   ├── application.yml                       # 应用配置
│   ├── application.yml.example               # 配置示例
│   ├── schema.sql                            # 数据库初始化脚本
│   ├── db/migration/                         # Flyway 迁移脚本
│   └── mapper/                               # MyBatis XML 映射文件
└── pom.xml                                   # Maven 配置

```

## 技术栈

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Spring AI**: 1.1.2
- **Spring AI Alibaba DashScope**: 1.1.2.1
- **MyBatis**: 3.0.3
- **MySQL**: 8.0+
- **DashVector**: 阿里云向量数据库
- **Bucket4j**: 8.10.1
- **JWT**: jjwt 0.12.6
- **jBCrypt**: 0.4

## 核心能力

- JWT 鉴权、注册/登录限流、统一异常处理
- 异步视频导入与导入状态追踪
- 视频字幕切分、MySQL 分片落库、DashVector 向量写入
- 会话管理、消息持久化、SSE 流式问答
- Query Rewrite、Hybrid Search、Rerank、动态 Top-K

## 当前 RAG 检索链路

```text
用户问题
  -> Query Rewrite
  -> Vector Search + Keyword Search
  -> Hybrid Merge
  -> Rerank
  -> Dynamic Top-K
  -> 拼接来源上下文
  -> LLM 流式回答
```

当前默认策略：

- `similarity-threshold=0.35`
- 新导入数据默认 `chunk-size=512`、`overlap-chars=128`
- 历史视频支持通过重建索引应用最新切分策略
- `keyword-top-k=8`
- `rerank-candidate-top-k=20`
- `model-rerank-enabled=false`
- `model-rerank-top-k=8`
- 动态 Top-K 默认 `3 / 5 / 8`

说明：

- 历史视频可以通过“重建索引”重新抓取字幕、重切分并重写向量索引
- `Rerank` 默认使用规则型实现，并支持按需开启模型式 rerank

## 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- DashVector 账号
- 阿里云百炼 API Key
- OpenAI 兼容接口

## 配置说明

### 1. 配置文件

建议先参考：

```text
src/main/resources/application.yml.example
```

然后在本地维护自己的 `application.yml`，至少确认以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/rag_bilibili?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:your_password}

  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: ${OPENAI_BASE_URL}
      chat:
        options:
          model: qwen3.6-plus

    dashscope:
      api-key: ${DASHSCOPE_API_KEY}

    alibaba:
      dashvector:
        api-key: ${DASHVECTOR_API_KEY}
        endpoint: ${DASHVECTOR_ENDPOINT}
        collection: bilibili
```

### 2. 环境变量

配置以下环境变量：

```powershell
$env:OPENAI_API_KEY="your_openai_api_key"
$env:OPENAI_BASE_URL="https://your-openai-compatible-endpoint"
$env:DASHSCOPE_API_KEY="your_dashscope_api_key"
$env:DASHVECTOR_API_KEY="your_dashvector_api_key"
$env:DASHVECTOR_ENDPOINT="your_dashvector_endpoint"
```

### 3. 初始化数据库

创建数据库：

```sql
CREATE DATABASE rag_bilibili CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

数据库结构默认由 Flyway 自动初始化，也可以参考：

- `src/main/resources/schema.sql`
- `src/main/resources/db/migration/V1__init.sql`

## 构建和运行

### 构建项目

```powershell
mvn clean package
```

### 运行项目

```powershell
mvn spring-boot:run
```

或者：

```powershell
java -jar target/rag-bilibili-server-1.0.0.jar
```

## API 接口

### 认证接口

- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/current` - 获取当前用户信息

### 视频管理接口

- `POST /api/videos` - 导入视频
- `POST /api/videos/{id}/rebuild` - 重建视频索引
- `GET /api/videos` - 获取视频列表
- `GET /api/videos/{id}` - 获取视频详情
- `DELETE /api/videos/{id}` - 删除视频

### 会话管理接口

- `POST /api/sessions` - 创建会话
- `GET /api/sessions` - 获取会话列表
- `GET /api/sessions/{id}` - 获取会话详情
- `DELETE /api/sessions/{id}` - 删除会话

### 对话接口

- `POST /api/sessions/{id}/messages/stream` - 流式发送消息（SSE）
- `GET /api/sessions/{id}/messages` - 获取消息列表

## 开发状态

### 已完成

- ✅ 用户认证、JWT 鉴权、限流和异常处理
- ✅ 异步视频导入、导入状态管理、失败原因回写
- ✅ 字幕切分、向量化、MySQL / DashVector 双写
- ✅ 单视频 / 全视频会话管理
- ✅ SSE 流式问答
- ✅ Query Rewrite
- ✅ Chunk overlap（新导入链路 + 历史视频重建索引）
- ✅ Hybrid Search（向量 + 关键词）
- ✅ 规则型 Rerank + 可配置模型式 Rerank
- ✅ 动态 Top-K
- ✅ 控制器、配置和部分服务层测试

## 测试

运行单元测试：

```powershell
mvn test
```

当前已覆盖：

- 控制器测试
- 配置测试
- 异常处理测试
- `ChatServiceImpl` 私有逻辑测试
- `VideoServiceImpl` 导入流程测试
- `ChunkDocumentSplitter` 切分逻辑测试

## 安全说明

- 所有敏感信息（API Keys、数据库密码）使用环境变量配置
- 请勿将包含真实密钥的文件提交到 Git
- 生产环境建议使用系统环境变量，不要依赖提交到仓库的本地配置文件
- 当前关键词召回基于 MySQL Full-Text `MATCH ... AGAINST`，如数据规模继续增大，建议升级为专用检索引擎

## 参考文档

- [系统需求规约](../系统需求规约.md)
- [概要设计](../概要设计.md)
- [详细设计](../详细设计.md)
- [向量化实现说明](../向量化实现说明.md)
