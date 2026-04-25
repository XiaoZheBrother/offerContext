# 部署文档

校招信息汇总平台生产环境部署指南。

## 环境要求

| 组件 | 最低版本 | 说明 |
|------|---------|------|
| JDK | 17+ | 后端运行环境 |
| MySQL | 5.7+ | 数据库 |
| Redis | 6.0+ | Magic Link Token 存储 + 频率限制 |
| Nginx | 1.18+ | 前端静态资源 + 反向代理 |
| Node.js | 18+ | 前端构建（仅部署时需要） |

## 一、数据库准备

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS cb-crawl-dev DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

> 如果使用现有爬虫数据库，可跳过此步骤。

### 2. 执行迁移

```bash
# V1: 基础表（1.0）
mysql -h <host> -u <user> -p <database> < campus-recruitment-backend/src/main/resources/db/migration/V1__create_new_tables.sql

# V2: 用户体系表（2.0）
mysql -h <host> -u <user> -p <database> < campus-recruitment-backend/src/main/resources/db/migration/V2__create_user_tables.sql
```

V1 迁移内容：
- `announcements` 表新增 `online_status` 列（默认1=上线）
- 更新 `expired_at` 为 NULL 的记录（设为 published_at + 90天）
- 创建 `page_views`、`click_logs`、`admin_users` 表
- 插入默认管理员（admin / admin123），**上线后请立即修改密码**

V2 迁移内容：
- 创建 `users` 表（C端用户，邮箱唯一）
- 创建 `favorites` 表（收藏，user_id + announcement_id 联合唯一）
- 创建 `application_records` 表（投递记录，状态流转）

## 二、后端部署

### 1. 编译打包

```bash
cd campus-recruitment-backend
mvn clean package -DskipTests
```

产出：`target/campus-recruitment-backend-1.0.0-SNAPSHOT.jar`

### 2. 配置环境变量

后端使用 `application-prod.yml`，通过环境变量注入敏感配置：

| 环境变量 | 说明 | 示例 |
|---------|------|------|
| `DB_URL` | 数据库连接 | `jdbc:mysql://127.0.0.1:3306/cb-crawl-dev?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8` |
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | `your_password` |
| `JWT_SECRET` | 管理端JWT签名密钥 | 随机32+位字符串 |
| `JWT_EXPIRATION` | 管理端JWT过期时间(ms) | `86400000`（默认24h） |
| `JWT_USER_SECRET` | 用户端JWT签名密钥 | 随机32+位字符串（需与管理端不同） |
| `JWT_USER_EXPIRATION` | 用户端JWT过期时间(ms) | `604800000`（默认7天） |
| `REDIS_HOST` | Redis 地址 | `127.0.0.1` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `REDIS_PASSWORD` | Redis 密码 | 空（无密码则不设） |

### 3. 启动服务

```bash
# 前台启动（调试用）
java -jar target/campus-recruitment-backend-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod

# 后台启动
nohup java -jar target/campus-recruitment-backend-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  > app.log 2>&1 &
```

### 4. Systemd 服务（推荐）

创建 `/etc/systemd/system/campus-recruitment.service`：

```ini
[Unit]
Description=Campus Recruitment Backend
After=network.target mysql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/campus-recruitment
ExecStart=/usr/bin/java -jar /opt/campus-recruitment/campus-recruitment-backend.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10

Environment=DB_URL=jdbc:mysql://127.0.0.1:3306/cb-crawl-dev?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8
Environment=DB_USERNAME=cb-crawl-dev
Environment=DB_PASSWORD=your_password
Environment=JWT_SECRET=your-admin-jwt-secret-key-at-least-32-chars
Environment=JWT_USER_SECRET=your-user-jwt-secret-key-at-least-32-chars
Environment=REDIS_HOST=127.0.0.1
Environment=REDIS_PORT=6379

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable campus-recruitment
sudo systemctl start campus-recruitment
```

## 三、前端部署

### 1. 构建

```bash
cd campus-recruitment-frontend
npm install
npm run build
```

产出：`dist/` 目录。

### 2. Nginx 配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态资源
    root /opt/campus-recruitment/frontend/dist;
    index index.html;

    # SPA 路由回退
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 静态资源缓存
    location /assets/ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # Gzip
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml;
    gzip_min_length 1024;
}
```

### 3. 部署静态文件

```bash
# 将构建产物复制到服务器
scp -r dist/* user@server:/opt/campus-recruitment/frontend/dist/

# 重载 Nginx
sudo nginx -t && sudo nginx -s reload
```

## 四、HTTPS（推荐）

使用 Let's Encrypt 免费证书：

```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

## 五、数据备份

```bash
# 每日全量备份（crontab）
0 3 * * * mysqldump -h <host> -u <user> -p<password> cb-crawl-dev > /backup/cb-crawl-dev_$(date +\%Y\%m\%d).sql
```

## 六、监控与日志

### 后端日志

```bash
# Systemd 日志
journalctl -u campus-recruitment -f

# 应用日志
tail -f /opt/campus-recruitment/app.log
```

### 健康检查

```bash
curl http://localhost:8080/api/announcements/filter-options
```

返回 200 即服务正常。

## 七、安全注意事项

1. **修改默认密码**：首次部署后立即登录后台修改 admin 密码
2. **JWT 密钥**：生产环境必须使用强随机密钥，管理端和用户端使用不同密钥
3. **Redis 安全**：生产环境建议设置密码，禁止公网访问
4. **数据库访问**：限制数据库只允许后端服务器 IP 访问
5. **HTTPS**：生产环境必须启用 HTTPS
6. **CORS**：生产环境在 SecurityConfig 中限制允许的域名
7. **敏感文件**：`.env`、`application-prod.yml` 不要提交到代码仓库
8. **频率限制**：/auth/send-magic-link 接口已内置 Redis 频率限制，防止暴力发送

## 八、2.0 新增部署说明

### Redis 降级策略

Redis 不可用时，/auth/send-magic-link 接口返回"登录服务暂时不可用，请稍后重试"，其他功能不受影响。

### Magic Link 开发模式

开发环境下（spring.profiles=dev），`POST /auth/send-magic-link` 接口直接在响应中返回生成的 token，无需发送邮件。前端会显示 token 供手动复制到验证接口测试。

生产环境需对接邮件服务（2.1 计划），将 `UserAuthService.sendMagicLink()` 中的 token 返回替换为邮件发送逻辑。
