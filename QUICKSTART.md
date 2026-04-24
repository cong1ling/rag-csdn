# 本地快速启动

本指南面向本地开发、自测联调和功能验收，不涉及生产部署。

说明：

- 当前业务域已经迁移为 **CSDN 文章 RAG**
- 后端主接口为 `/api/articles`
- 前端主页面为 `/articles`

## 1. 前置条件

本地建议准备以下环境：

- Java 17+
- Maven 3.6+
- Node.js 18+
- npm 9+
- MySQL 8.0+
- 可用的 OpenAI 兼容聊天模型服务
- 阿里云 DashScope API Key
- 阿里云 DashVector 实例

## 2. 启动后端

进入后端目录：

```powershell
cd rag-csdn-server
```

当前后端依赖以下环境变量，建议配置到系统环境变量、IDE 运行配置，或启动终端会话中：

```env
DB_USERNAME=root
DB_PASSWORD=123456
JWT_SECRET=replace_with_a_long_random_secret
OPENAI_API_KEY=your_openai_api_key
OPENAI_BASE_URL=https://api.openai.com
DASHSCOPE_API_KEY=your_dashscope_api_key
DASHVECTOR_API_KEY=your_dashvector_api_key
DASHVECTOR_ENDPOINT=your_dashvector_endpoint
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

说明：

- 默认数据库连接指向 `rag_csdn`
- JDBC URL 已开启 `createDatabaseIfNotExist=true`，若当前账号没有建库权限，请先手动创建数据库
- Flyway 会自动执行迁移，新库会直接落到 `article / chunk / vector_mapping / session / message` 结构

启动命令：

```powershell
mvn spring-boot:run
```

默认访问地址：

```text
http://localhost:8080
```

可选联调页：

```text
http://localhost:8080/dev.html
```

## 3. 启动前端

打开第二个终端，进入前端目录：

```powershell
cd rag-csdn-front
```

复制前端环境变量模板：

```powershell
Copy-Item .env.example .env
```

如使用 Bash，可执行：

```bash
cp .env.example .env
```

默认前端环境变量如下：

```env
VITE_API_BASE_URL=/api
VITE_PROXY_TARGET=http://localhost:8080
```

安装依赖并启动：

```powershell
npm install
npm run dev
```

默认访问地址：

```text
http://localhost:5173
```

## 4. 本地联调链路

启动完成后，默认联调链路如下：

1. 浏览器访问 `http://localhost:5173`
2. 前端把 `/api/*` 请求代理到 `http://localhost:8080`
3. 登录成功后，前端把 JWT 保存在 `localStorage`
4. 后续请求通过 `Authorization: Bearer <token>` 访问后端
5. 聊天接口通过 SSE 持续接收流式回答

## 5. 启动后验证

可以按下面的顺序快速验证：

1. 打开首页，确认页面可正常加载
2. 注册并登录账号
3. 进入 `/import` 导入一篇 CSDN 文章
4. 进入 `/articles` 查看导入状态与分片数量
5. 创建会话并在 `/chat/:sessionId` 验证流式问答

## 6. 常见问题

### 6.1 后端启动失败

优先检查：

- MySQL 是否可连接
- `JWT_SECRET` 是否已配置
- `OPENAI_API_KEY`、`DASHSCOPE_API_KEY`、`DASHVECTOR_*` 是否已配置
- DashVector Collection `csdn` 是否已准备完成

### 6.2 前端能打开，但接口全部失败

优先检查：

- 后端是否运行在 `8080`
- `.env` 中的 `VITE_PROXY_TARGET` 是否正确
- 浏览器请求头里是否带上了 `Authorization: Bearer <token>`
- 后端 `CORS_ALLOWED_ORIGINS` 是否包含 `http://localhost:5173`

### 6.3 聊天没有流式输出

优先检查：

- `POST /api/sessions/{sessionId}/messages/stream` 是否可访问
- OpenAI 兼容模型服务是否可连通
- 浏览器控制台是否有鉴权或网络错误

## 7. 文档索引

- 后端启动说明：`rag-csdn-server/后端启动配置文档.md`
- 前端启动说明：`rag-csdn-front/前端快速启动文档.md`
- 部署说明：`DEPLOYMENT.md`

