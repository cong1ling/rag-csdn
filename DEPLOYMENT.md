# RAG-CSDN 部署指南

说明：

- 仓库目录名和部分部署文件名仍保留 `rag-bilibili`
- 当前后端业务已切换为 CSDN 文章 RAG
- 后端主接口已切换为 `/api/articles`

## 架构说明

- **前端**：部署在 Cloudflare Pages（静态托管）
- **后端**：部署在阿里云 ECS（Spring Boot 应用）
- **数据库**：阿里云 RDS MySQL 或 ECS 自建 MySQL
- **向量数据库**：阿里云 DashVector

---

## 一、后端部署（阿里云 ECS）

### 1.1 准备工作

#### 购买阿里云资源
- ECS 服务器（推荐配置：2核4G，Ubuntu 22.04）
- RDS MySQL 8.0（或在 ECS 上自建）
- DashVector 向量数据库实例
- 域名并完成备案（如 api.yourdomain.com）

#### 安装依赖
```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装 Java 17
sudo apt install openjdk-17-jdk -y
java -version

# 安装 Maven（用于构建）
sudo apt install maven -y

# 安装 MySQL（如果不使用 RDS）
sudo apt install mysql-server -y
sudo mysql_secure_installation
```

### 1.2 配置数据库

```bash
# 登录 MySQL
sudo mysql -u root -p

# 创建数据库和用户
CREATE DATABASE rag_bilibili CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'raguser'@'%' IDENTIFIED BY 'your_strong_password';
GRANT ALL PRIVILEGES ON rag_bilibili.* TO 'raguser'@'%';
FLUSH PRIVILEGES;
EXIT;
```

### 1.3 配置环境变量

创建 `/etc/systemd/system/rag-csdn.service`：

```ini
[Unit]
Description=RAG CSDN Server
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/rag-csdn-server
ExecStart=/usr/bin/java -jar /home/ubuntu/rag-csdn-server/rag-csdn-server-1.0.0.jar
Restart=on-failure
RestartSec=10

# 环境变量
Environment="DB_USERNAME=raguser"
Environment="DB_PASSWORD=your_strong_password"
Environment="OPENAI_API_KEY=sk-xxx"
Environment="OPENAI_BASE_URL=https://api.deepseek.com"
Environment="DASHSCOPE_API_KEY=sk-xxx"
Environment="DASHVECTOR_API_KEY=xxx"
Environment="DASHVECTOR_ENDPOINT=xxx"
Environment="SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/rag_bilibili?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"

[Install]
WantedBy=multi-user.target
```

### 1.4 构建和部署

在本地构建：
```bash
cd rag-csdn-server
mvn clean package -DskipTests
```

上传到服务器：
```bash
scp target/rag-csdn-server-1.0.0.jar ubuntu@your-server-ip:/home/ubuntu/rag-csdn-server/
```

启动服务：
```bash
sudo systemctl daemon-reload
sudo systemctl enable rag-csdn
sudo systemctl start rag-csdn
sudo systemctl status rag-csdn
```

查看日志：
```bash
sudo journalctl -u rag-csdn -f
```

### 1.5 配置 Nginx 反向代理

安装 Nginx：
```bash
sudo apt install nginx -y
```

创建配置文件 `/etc/nginx/sites-available/rag-csdn`：

```nginx
server {
    listen 80;
    server_name api.yourdomain.com;

    # CORS 配置（允许 Cloudflare Pages 访问）
    add_header 'Access-Control-Allow-Origin' 'https://your-app.pages.dev' always;
    add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, DELETE, OPTIONS' always;
    add_header 'Access-Control-Allow-Headers' 'Content-Type, Authorization' always;
    add_header 'Access-Control-Allow-Credentials' 'true' always;

    # 处理 OPTIONS 预检请求
    if ($request_method = 'OPTIONS') {
        return 204;
    }

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # SSE 支持
        proxy_buffering off;
        proxy_cache off;
        proxy_set_header Connection '';
        proxy_http_version 1.1;
        chunked_transfer_encoding on;
    }
}
```

启用配置：
```bash
sudo ln -s /etc/nginx/sites-available/rag-csdn /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

### 1.6 配置 HTTPS（使用 Let's Encrypt）

```bash
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d api.yourdomain.com
```

Certbot 会自动修改 Nginx 配置并添加 SSL 证书。

---

## 二、前端部署（Cloudflare Pages）

### 2.1 准备构建配置

创建 `.env.production` 文件：

```bash
cd rag-csdn-front
cat > .env.production << EOF
VITE_API_BASE_URL=https://api.yourdomain.com/api
EOF
```

### 2.2 本地构建测试

```bash
npm install
npm run build
```

构建产物在 `dist` 目录。

### 2.3 部署到 Cloudflare Pages

#### 方式一：通过 Git 自动部署（推荐）

1. 将代码推送到 GitHub/GitLab
2. 登录 [Cloudflare Dashboard](https://dash.cloudflare.com/)
3. 进入 **Pages** → **Create a project**
4. 连接你的 Git 仓库
5. 配置构建设置：
   - **Framework preset**: Vue
   - **Build command**: `cd rag-csdn-front && npm install && npm run build`
   - **Build output directory**: `rag-csdn-front/dist`
   - **Root directory**: `/`
   - **Environment variables**:
     - `VITE_API_BASE_URL` = `https://api.yourdomain.com/api`

6. 点击 **Save and Deploy**

#### 方式二：使用 Wrangler CLI 手动部署

```bash
# 安装 Wrangler
npm install -g wrangler

# 登录 Cloudflare
wrangler login

# 部署
cd rag-csdn-front
npm run build
wrangler pages deploy dist --project-name=rag-csdn
```

### 2.4 配置自定义域名

1. 在 Cloudflare Pages 项目设置中，进入 **Custom domains**
2. 添加你的域名（如 `app.yourdomain.com`）
3. Cloudflare 会自动配置 DNS 和 SSL

---

## 三、CORS 配置更新

部署完成后，需要更新后端的 CORS 配置以允许 Cloudflare Pages 域名访问。

### 3.1 创建 CORS 配置类

在后端项目中创建 `src/main/java/com/example/ragcsdn/config/CorsConfig.java`：

```java
package com.example.ragcsdn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许的前端域名
        config.addAllowedOrigin("https://your-app.pages.dev");
        config.addAllowedOrigin("https://app.yourdomain.com");

        // 开发环境
        config.addAllowedOrigin("http://localhost:5173");

        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
```

### 3.2 更新 Nginx 配置

修改 `/etc/nginx/sites-available/rag-csdn`，将 CORS 头中的域名替换为实际的 Cloudflare Pages 域名：

```nginx
add_header 'Access-Control-Allow-Origin' 'https://your-app.pages.dev' always;
```

重启 Nginx：
```bash
sudo systemctl restart nginx
```

---

## 四、环境变量清单

### 后端环境变量

| 变量名 | 说明 | 示例 |
|--------|------|------|
| `DB_USERNAME` | 数据库用户名 | `raguser` |
| `DB_PASSWORD` | 数据库密码 | `your_password` |
| `OPENAI_API_KEY` | OpenAI API Key | `sk-xxx` |
| `OPENAI_BASE_URL` | OpenAI API 地址 | `https://api.deepseek.com` |
| `DASHSCOPE_API_KEY` | 阿里云 DashScope Key | `sk-xxx` |
| `DASHVECTOR_API_KEY` | DashVector API Key | `xxx` |
| `DASHVECTOR_ENDPOINT` | DashVector 端点 | `xxx` |

### 前端环境变量

| 变量名 | 说明 | 示例 |
|--------|------|------|
| `VITE_API_BASE_URL` | 后端 API 地址 | `https://api.yourdomain.com/api` |

---

## 五、验证部署

### 5.1 检查后端健康状态

```bash
curl https://api.yourdomain.com/api/health
```

### 5.2 检查前端访问

访问 `https://your-app.pages.dev` 或 `https://app.yourdomain.com`

### 5.3 测试完整流程

1. 注册账号
2. 登录
3. 导入文章
4. 创建会话
5. 发送消息

---

## 六、常见问题

### 6.1 CORS 错误

**症状**：浏览器控制台显示 CORS 错误

**解决**：
1. 检查后端 `CorsConfig.java` 中是否添加了前端域名
2. 检查 Nginx 配置中的 CORS 头
3. 确保前端使用 HTTPS（Cloudflare Pages 默认启用）

### 6.2 Session 无法保持

**症状**：登录后立即退出

**解决**：
1. 确保前端请求已正确携带 `Authorization: Bearer <token>`
2. 确保后端与反向代理没有丢弃 `Authorization` 头
3. 前后端必须都使用 HTTPS（或都使用 HTTP）

### 6.3 SSE 流式响应中断

**症状**：AI 回答只显示部分内容

**解决**：
1. 检查 Nginx 配置中的 `proxy_buffering off`
2. 增加超时时间：`proxy_read_timeout 300s;`

---

## 七、监控和维护

### 7.1 日志查看

```bash
# 后端日志
sudo journalctl -u rag-csdn -f

# Nginx 日志
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

### 7.2 自动备份数据库

创建备份脚本 `/home/ubuntu/backup-db.sh`：

```bash
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
mysqldump -u raguser -p'your_password' rag_bilibili > /home/ubuntu/backups/rag_bilibili_$DATE.sql
find /home/ubuntu/backups -name "*.sql" -mtime +7 -delete
```

添加到 crontab：
```bash
crontab -e
# 每天凌晨 2 点备份
0 2 * * * /home/ubuntu/backup-db.sh
```

---

## 八、成本估算

- **阿里云 ECS**：2核4G，约 ¥100-200/月
- **RDS MySQL**：基础版，约 ¥50-100/月（或使用 ECS 自建免费）
- **DashVector**：按使用量计费
- **Cloudflare Pages**：免费（每月 500 次构建）
- **域名**：约 ¥50-100/年

**总计**：约 ¥150-300/月

---

## 九、安全建议

1. **使用强密码**：数据库、API Key 等
2. **启用防火墙**：只开放必要端口（80, 443, 22）
3. **定期更新**：系统和依赖包
4. **限流保护**：后端已集成 Bucket4j 限流
5. **备份策略**：定期备份数据库和配置文件
6. **监控告警**：使用阿里云监控服务

---

## 十、更新部署

### 更新后端

```bash
# 本地构建
cd rag-csdn-server
mvn clean package -DskipTests

# 上传到服务器
scp target/rag-csdn-server-1.0.0.jar ubuntu@your-server-ip:/home/ubuntu/rag-csdn-server/

# 重启服务
ssh ubuntu@your-server-ip
sudo systemctl restart rag-csdn
```

### 更新前端

如果使用 Git 自动部署，只需推送代码到仓库，Cloudflare Pages 会自动构建部署。

如果使用手动部署：
```bash
cd rag-csdn-front
npm run build
wrangler pages deploy dist --project-name=rag-csdn
```

