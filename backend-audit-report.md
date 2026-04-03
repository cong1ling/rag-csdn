# RAG-Bilibili 后端代码审查报告

> 审查范围：`rag-bilibili-server` 全部 Java 源码、MyBatis XML、SQL Schema、配置文件  
> 审查时间：2026-04-03

---

## 一、🔴 高风险问题（建议立即修复）

### 1. RateLimiter 内存泄漏
**文件**：`util/RateLimiter.java`  
**问题**：`REGISTER_BUCKETS` 和 `LOGIN_BUCKETS` 两个 `ConcurrentHashMap` 只增不减。每个不同的 IP 都会创建一个 Bucket 对象永久驻留内存。在公网部署场景下，攻击者可以通过伪造大量不同 IP（或经过代理后真实产生大量 IP）耗尽服务器内存。  
**建议**：
- 方案 A：使用 Caffeine/Guava Cache 替代 ConcurrentHashMap，设置 `expireAfterAccess(1, TimeUnit.HOURS)` 自动淘汰。
- 方案 B：添加定时任务清理过期的 Bucket。

### 2. X-Forwarded-For 信任链未限制
**文件**：`controller/AuthController.java:78-88`  
**问题**：`getClientIp()` 直接信任 `X-Forwarded-For` 头。在没有反向代理或代理未正确配置的部署环境中，客户端可以伪造此 Header 绕过 IP 限流。  
**建议**：
- 仅在确认存在受信任的反向代理时才读取 `X-Forwarded-For`。
- 使用 Spring Boot 的 `server.forward-headers-strategy=NATIVE` 配合 `server.tomcat.remoteip.internal-proxies` 限制受信任代理 IP 段。

### 3. JWT Secret 未设默认安全值
**文件**：`util/JwtUtil.java`，`application.yml.example` 中缺少 `jwt.secret` 和 `jwt.expiration`  
**问题**：`application.yml.example` 中没有定义 `jwt.secret` 和 `jwt.expiration`，如果开发者忘记配置，启动时会直接报错（无默认值）。更危险的是，如果配置了一个过短的 secret（少于 32 字节），HMAC-SHA 签名的安全性会大幅降低。  
**建议**：
- 在 `application.yml.example` 中明确列出 `jwt.secret` 和 `jwt.expiration` 的占位值和长度要求。
- 在 `JwtUtil` 构造函数中添加 `Assert` 校验 secret 长度（≥ 32 字节）。

### 4. 视频导入是同步阻塞操作
**文件**：`service/impl/VideoServiceImpl.java:66-188`  
**问题**：`importVideo()` 在 HTTP 请求线程中同步完成整个流程：调用 Bilibili API 拉取字幕 → 文本切分 → 向量化写入 DashVector → 批量入库。对于长视频或网络延迟大的情况，请求可能持续数十秒甚至超时。前端也无法得知进度。  
**建议**：
- 将导入改为异步任务：Controller 接收请求后立即返回 `IMPORTING` 状态，后台线程池异步执行导入流程。
- 提供轮询接口 `GET /api/videos/{id}/status` 或 WebSocket 通知前端导入进度。

### 5. Bilibili Cookies 在请求体中明文传输
**文件**：`dto/request/ImportVideoRequest.java`  
**问题**：`sessdata`、`biliJct`、`buvid3` 作为请求体字段直接传输。虽然 HTTPS 下传输是加密的，但这些敏感凭据会被记录到：
- 服务端的请求日志（如果开启了 request body 日志）
- 前端的浏览器开发者工具网络面板（任何有物理访问权限的人可见）
- 潜在的日志聚合系统

**建议**：
- 考虑将 Cookies 在服务端加密存储，用户只需配置一次。后续导入时通过 userId 查找已存储的凭据。
- 如果必须每次传递，确保服务端日志不会打印请求体中的敏感字段（可添加 `@JsonProperty(access = WRITE_ONLY)` 或自定义日志过滤）。

---

## 二、🟡 中等风险 / 可优化处

### 6. GlobalExceptionHandler 中 BusinessException 未设置 HTTP 状态码
**文件**：`exception/GlobalExceptionHandler.java:36-40`  
**问题**：所有 `BusinessException` 返回的 HTTP 状态码都是 200（Spring 默认）。前端虽然通过 `code` 字段判断业务错误，但这不符合 RESTful 最佳实践，也会导致：
- 监控系统（Prometheus/Grafana）无法通过 HTTP 状态码统计错误率
- API 网关的重试/熔断策略失效（因为看到的都是 200）

**建议**：为 `BusinessException` 映射合适的 HTTP 状态码：
- `NOT_LOGGED_IN` → 401
- `RATE_LIMIT_EXCEEDED` → 429
- `*_NOT_FOUND` → 404
- `PARAM_ERROR` → 400
- `SYSTEM_ERROR` → 500

### 7. UserContext (ThreadLocal) 在异步场景下丢失
**文件**：`util/UserContext.java`，`service/impl/ChatServiceImpl.java:91`  
**问题**：`ChatServiceImpl.streamMessage()` 中，`taskExecutor.execute(() -> { ... })` 在子线程中执行。但 `UserContext` 基于 `ThreadLocal`，子线程无法访问到主线程设置的 userId。当前代码碰巧没有在子线程中调用 `UserContext.get()`（userId 作为参数传递了），但如果未来有人在异步块内调用依赖 `UserContext` 的方法，将返回 null。  
**建议**：
- 使用 `InheritableThreadLocal` 替代 `ThreadLocal`（注意线程池场景下需配合 `TaskDecorator`）。
- 或者在 `TaskExecutorConfig` 中配置 `TaskDecorator` 来传递上下文。

### 8. SessionServiceImpl.convertToResponse 存在 N+1 查询
**文件**：`service/impl/SessionServiceImpl.java:101-117`  
**问题**：`listSessions()` 先查询所有会话，再在 `convertToResponse()` 中对每个会话执行 `videoMapper.selectById()` 查询视频标题。如果用户有 N 个会话，就会产生 1 + N 次数据库查询。  
**建议**：
- 在 SQL 层使用 `LEFT JOIN video ON session.video_id = video.id` 一次性查出视频标题。
- 或者在 Service 层批量查询所有关联的 videoId，用 Map 缓存后填充。

### 9. VideoServiceImpl.convertToResponse 同样存在 N+1
**文件**：`service/impl/VideoServiceImpl.java:253-268`  
**问题**：`listVideos()` 中每个视频都执行 `chunkMapper.countByVideoId()` 查询分片数量。  
**建议**：在 SQL 层使用 `LEFT JOIN` + `COUNT` 子查询，或者在 Video 表中添加冗余字段 `chunk_count`。

### 10. SSE 超时时间过短
**文件**：`service/impl/ChatServiceImpl.java:68`  
**问题**：`SSE_TIMEOUT = 60000L`（60 秒）。如果 LLM 响应较慢（如高峰期排队），或者上下文较长需要较长处理时间，60 秒可能不够。  
**建议**：至少设置为 120-180 秒，或者通过配置文件可调。

### 11. 缺少 CORS 配置
**文件**：`config/WebConfig.java`  
**问题**：没有显式的 CORS 配置。如果前后端分离部署在不同域名/端口，浏览器会阻止跨域请求。当前可能是通过 Vite 代理解决的开发环境问题，但生产环境需要正式的 CORS 策略。  
**建议**：在 `WebConfig` 中添加 `addCorsMappings()` 配置，或在 `application.yml` 中外部化 CORS 允许的域名列表。

### 12. DuplicateKeyException 处理过于笼统
**文件**：`exception/GlobalExceptionHandler.java:27-31`  
**问题**：所有 `DuplicateKeyException` 都映射为"用户名已存在"。但系统中还有 `uk_user_bvid`（视频表）、`uk_vector_id`（向量映射表）等唯一键约束，这些冲突也会被错误地提示为"用户名已存在"。  
**建议**：通过分析异常消息中的表名/索引名来区分不同的唯一键冲突，或者在业务层通过先查询再插入避免依赖数据库异常。

---

## 三、🟢 代码质量 & 最佳实践建议

### 13. 使用构造器注入替代 @Autowired 字段注入
**涉及文件**：几乎所有 Controller、Service、Config 类  
**问题**：全部使用 `@Autowired` 字段注入。Spring 官方推荐构造器注入，优势：
- 依赖关系更明确（编译时检查）
- 支持 `final` 字段（不可变性）
- 方便单元测试（无需反射）

### 14. UserServiceImpl.logout() 是空方法
**文件**：`service/impl/UserServiceImpl.java:62-65`  
**问题**：注释说"Session 认证方式，登出由 Controller 处理 session.invalidate()"，但实际系统使用的是 JWT 认证。这个方法和注释都已过时。  
**建议**：删除此方法，或在接口层去掉 `logout` 的定义。当前 Controller 的 logout 已经正确实现为无状态返回。

### 15. 日期格式化器重复定义
**文件**：`UserServiceImpl`、`VideoServiceImpl`、`SessionServiceImpl`、`MessageServiceImpl`  
**问题**：四个 Service 都各自定义了 `private static final DateTimeFormatter FORMATTER`，完全相同的格式。  
**建议**：提取到公共常量类中（如 `Constants.DATE_FORMATTER`），避免重复。

### 16. 配置文件中开启了 MyBatis SQL 日志
**文件**：`application.yml.example:55`  
**问题**：`log-impl: org.apache.ibatis.logging.stdout.StdOutImpl` 会将所有 SQL 打印到控制台。在生产环境中：
- 大量日志输出影响性能
- 可能暴露数据库表结构和查询数据

**建议**：生产环境注释掉或使用 profile 区分：
```yaml
# 开发环境
mybatis.configuration.log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

# 生产环境
spring.profiles: prod
# 不配置 log-impl
```

### 17. application.yml.example 残留 session 配置
**文件**：`application.yml.example:59-61`  
**问题**：`server.servlet.session.timeout: 30m` 是 Session 认证的遗留配置，当前系统已改用 JWT，此配置无实际作用。  
**建议**：删除以避免混淆。

### 18. VectorIDGenerator.parse() 的脆弱性
**文件**：`util/VectorIDGenerator.java:28-30`  
**问题**：`parse()` 使用 `split("_", 3)` 解析向量 ID，但 BV 号本身不含下划线，分割是安全的。然而如果未来用户名或其他含 `_` 的字段被加入 ID 格式，就会出错。目前未被调用，属于潜在风险。  
**建议**：如果不需要此方法，可以删除。如果需要，建议添加输入校验。

### 19. 删除视频时缺少对 ALL_VIDEOS 类型会话的处理
**文件**：`service/impl/VideoServiceImpl.java:207-251`  
**问题**：`deleteVideo()` 只删除 `session.video_id = videoId` 的会话（即单视频会话）。`ALL_VIDEOS` 类型的会话不受影响，但该会话中引用此视频内容的历史消息会变成"幽灵引用"——向量已删除，但聊天记录中仍保留了基于该视频的回答。  
**建议**：这是一个产品层面的决策。至少在删除视频时提示用户"此视频的相关对话内容可能不再准确"。

### 20. 消息表缺少外键约束
**文件**：`schema.sql`  
**问题**：所有表之间没有外键约束（`session.user_id` → `user.id` 等）。虽然这在性能和运维上有好处（更灵活），但也意味着数据一致性完全依赖应用层逻辑。如果代码 Bug 导致某些关联删除遗漏，数据库中会残留孤儿记录。  
**建议**：当前的应用层级联删除逻辑是完整的（`deleteVideo` 中按序清理了所有关联表），保持现状即可，但建议添加一个定期数据一致性检查的任务。

---

## 四、总结

| 优先级 | 数量 | 关键项 |
|--------|------|--------|
| 🔴 高风险 | 5 | RateLimiter 内存泄漏、IP 伪造绕过限流、JWT Secret 校验、同步阻塞导入、Cookie 明文传输 |
| 🟡 中等 | 7 | HTTP 状态码缺失、ThreadLocal 异步丢失、N+1 查询、SSE 超时、CORS、异常处理笼统 |
| 🟢 改进 | 8 | 构造器注入、死代码清理、常量提取、生产日志配置、残留配置 |

**整体评价**：代码结构清晰，分层合理，异常处理和错误码体系完善。主要风险集中在安全性（限流绕过、凭据处理）和性能（同步导入、N+1 查询）两个维度。建议优先处理 🔴 高风险项。
