# 快速部署指南

本指南帮助你在 30 分钟内完成 RAG-Bilibili 的部署。

## 前置条件

- 阿里云 ECS 服务器（2核4G，Ubuntu 22.04）
- 阿里云 RDS MySQL 或自建 MySQL
- 阿里云 DashVector 向量数据库
- Cloudflare 账号
- 域名（已备案）

## 快速部署步骤

### 第一步：准备后端服务器（10分钟）

```bash
# SSH 登录到阿里云 ECS
ssh ubuntu@your-server-ip

# 安装 Java 17
sudo apt update && sudo apt upgrade -y
sudo apt install openjdk-17-jdk -y

# 安装 MySQL（如果不使用 RDS）
sudo apt install mysql-server -y
sudo mysql_secure_installation

# 创建数据库
sudo mysql -u root -p
CREATE DATABASE rag_bilibili CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'raguser'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON rag_bilibili.* TO 'raguser'@'%';
FLUSH PRIVILEGES;
EXIT;

# 安装 Nginx
sudo apt install nginx certbot python3-certbot-nginx -y
```

### 第二步：配置后端服务（5分钟）

```bash
# 在服务器上创建目录
mkdir -p /home/ubuntu/rag-bilibili-server

# 在本地修改配置文件
# 编辑 deployment/rag-bilibili.service，填入实际的：
# - 数据库密码
# - OPENAI_API_KEY
# - DASHSCOPE_API_KEY
# - DASHVECTOR_API_KEY
# - DASHVECTOR_ENDPOINT
# - CORS_ALLOWED_ORIGINS（你的 Cloudflare Pages 域名）

# 上传配置文件到服务器
scp deployment/rag-bilibili.service ubuntu@your-server-ip:/tmp/
ssh ubuntu@your-server-ip "sudo mv /tmp/rag-bilibili.service /etc/systemd/system/"
```

### 第三步：部署后端（5分钟）

```bash
# 在本地项目根目录运行
cd rag-bilibili-server
mvn clean package -DskipTests

# 上传 JAR 包到服务器
scp target/rag-bilibili-server-1.0.0.jar ubuntu@your-server-ip:/home/ubuntu/rag-bilibili-server/

# 在服务器上启动服务
ssh ubuntu@your-server-ip
sudo systemctl daemon-reload
sudo systemctl enable rag-bilibili
sudo systemctl start rag-bilibili
sudo systemctl status rag-bilibili

# 查看日志确认启动成功
sudo journalctl -u rag-bilibili -f
```

### 第四步：配置 Nginx 和 HTTPS（5分钟）

```bash
# 在本地修改 Nginx 配置
# 编辑 deployment/nginx.conf，将以下内容替换为实际值：
# - api.yourdomain.com（你的后端域名）
# - your-app.pages.dev（你的 Cloudflare Pages 域名）

# 上传 Nginx 配置
scp deployment/nginx.conf ubuntu@your-server-ip:/tmp/
ssh ubuntu@your-server-ip "sudo mv /tmp/nginx.conf /etc/nginx/sites-available/rag-bilibili"

# 启用配置
ssh ubuntu@your-server-ip
sudo ln -s /etc/nginx/sites-available/rag-bilibili /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx

# 配置 HTTPS 证书
sudo certbot --nginx -d api.yourdomain.com
```

### 第五步：部署前端到 Cloudflare Pages（5分钟）

#### 方式 A：通过 Git 自动部署（推荐）

1. 将代码推送到 GitHub
2. 登录 [Cloudflare Dashboard](https://dash.cloudflare.com/)
3. 进入 **Pages** → **Create a project**
4. 连接 GitHub 仓库
5. 配置构建设置：
   - **Build command**: `cd rag-bilibili-front && npm install && npm run build`
   - **Build output directory**: `rag-bilibili-front/dist`
   - **Environment variables**:
     - `VITE_API_BASE_URL` = `https://api.yourdomain.com/api`
6. 点击 **Save and Deploy**

#### 方式 B：使用 CLI 手动部署

```bash
# 安装 Wrangler CLI
npm install -g wrangler

# 登录 Cloudflare
wrangler login

# 创建 .env.production
cd rag-bilibili-front
cat > .env.production << EOF
VITE_API_BASE_URL=https://api.yourdomain.com/api
EOF

# 构建并部署
npm install
npm run build
wrangler pages deploy dist --project-name=rag-bilibili
```

### 第六步：验证部署（5分钟）

1. **测试后端 API**
   ```bash
   curl https://api.yourdomain.com/api/auth/login
   ```

2. **访问前端**
   - 打开浏览器访问 `https://your-app.pages.dev`
   - 注册账号
   - 登录系统
   - 导入视频测试

3. **检查 CORS**
   - 打开浏览器开发者工具（F12）
   - 查看 Network 标签，确认没有 CORS 错误

## 常见问题快速修复

### 问题 1：CORS 错误

```bash
# 检查后端 CORS 配置
ssh ubuntu@your-server-ip
sudo journalctl -u rag-bilibili | grep CORS

# 检查 Nginx CORS 配置
sudo cat /etc/nginx/sites-available/rag-bilibili | grep -A 5 "Access-Control"

# 重启服务
sudo systemctl restart rag-bilibili
sudo systemctl restart nginx
```

### 问题 2：无法登录（JWT / 鉴权问题）

确保：
1. 前后端都使用 HTTPS
2. 前端请求头已正确携带 `Authorization: Bearer <token>`
3. 后端未被反向代理、CORS 或网关策略拦掉 `Authorization` 头

### 问题 3：SSE 流式响应中断

```bash
# 检查 Nginx 配置
ssh ubuntu@your-server-ip
sudo cat /etc/nginx/sites-available/rag-bilibili | grep -A 3 "proxy_buffering"

# 应该包含：
# proxy_buffering off;
# proxy_cache off;
```

## 使用自动化脚本

后续更新可以使用自动化脚本：

```bash
# 部署后端
chmod +x deployment/deploy-backend.sh
./deployment/deploy-backend.sh

# 部署前端
chmod +x deployment/deploy-frontend.sh
./deployment/deploy-frontend.sh

# 一键部署
chmod +x deployment/deploy-all.sh
./deployment/deploy-all.sh
```

## 监控和维护

```bash
# 查看后端日志
ssh ubuntu@your-server-ip 'sudo journalctl -u rag-bilibili -f'

# 查看 Nginx 日志
ssh ubuntu@your-server-ip 'sudo tail -f /var/log/nginx/access.log'

# 重启服务
ssh ubuntu@your-server-ip 'sudo systemctl restart rag-bilibili'

# 备份数据库
ssh ubuntu@your-server-ip '/home/ubuntu/backup-db.sh'
```

## 下一步

- 配置自定义域名（在 Cloudflare Pages 设置中）
- 设置数据库自动备份（crontab）
- 配置监控告警（阿里云监控）
- 优化性能（CDN、缓存等）

## 需要帮助？

查看完整文档：`DEPLOYMENT.md`
