# Campus Recruitment Backend

校招信息汇总平台后端服务，基于 Spring Boot 3.2 构建，提供校招公告浏览、筛选、用户认证、收藏、投递记录、后台管理等 API。

## 技术栈

- **Java 17** + **Spring Boot 3.2.0**
- **Spring Data JPA** + Hibernate（MySQL 5.7）
- **Spring Data Redis**（Magic Link Token + 频率限制）
- **Spring Boot Mail** + QQ 邮箱 SMTP（Magic Link 邮件发送）
- **Spring Security** + 双 JWT（管理端 + 用户端 jjwt 0.12.x）
- **AntiSamy** XSS 防护
- **Lombok**
- **Maven**

## 项目结构

```
src/main/java/com/campus/recruitment/
├── Application.java          # 启动类
├── common/                   # 通用类（ApiResponse, PageResponse）
├── config/                   # 配置类（SecurityConfig, JwtAuthenticationFilter, RateLimitFilter）
├── controller/
│   ├── AnnouncementController.java   # C端：公告列表/详情/筛选
│   ├── AuthController.java           # C端：Magic Link 登录/验证/登出/用户信息
│   ├── FavoriteController.java       # C端：收藏增删查
│   ├── ApplicationController.java    # C端：投递记录 toggle/CRUD
│   ├── TrackingController.java       # C端：点击/访问埋点
│   └── admin/                        # 后台管理
│       ├── AdminAuthController.java          # 登录
│       ├── AdminAnnouncementController.java  # 公告CRUD/上下线
│       └── AdminStatisticsController.java    # 统计数据
├── dto/
│   ├── ApplyStatus.java              # 申请状态枚举
│   ├── request/                      # 请求DTO
│   └── response/                     # 响应DTO
├── entity/                   # JPA实体（20+个，含 User, Favorite, ApplicationRecord）
├── enums/                    # 枚举（ApplicationStatus: APPLIED/WRITTEN_TEST/INTERVIEW/OFFER/REJECTED）
├── exception/                # 异常处理
├── repository/               # JPA Repository
├── service/                  # 业务逻辑（含 EmailService 邮件发送）
└── util/                     # 工具类（XssUtils, UrlValidator, IpUtil）
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.3+
- MySQL 5.7+
- Redis 6.0+

### 配置

编辑 `src/main/resources/application-dev.yml`，修改数据库和 Redis 连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://<host>:<port>/<database>?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
    username: <username>
    password: <password>
  data:
    redis:
      host: 127.0.0.1
      port: 6379
  mail:
    host: smtp.qq.com
    port: 587
    username: <your-qq-email>
    password: <your-smtp-authorization-code>

app:
  mail:
    from: "校招信息汇总 <<your-qq-email>>"
    base-url: http://localhost:3000
```

JWT 密钥在 `application.yml` 中配置，生产环境务必通过环境变量覆盖。

### 数据库迁移

首次运行前，执行迁移脚本：

```bash
mysql -h <host> -u <user> -p <database> < src/main/resources/db/migration/V1__create_new_tables.sql
mysql -h <host> -u <user> -p <database> < src/main/resources/db/migration/V2__create_user_tables.sql
```

V1 脚本会：
- 为 `announcements` 表新增 `online_status` 列
- 更新 `expired_at` 为 NULL 的记录（默认 published_at + 90 天）
- 创建 `page_views`、`click_logs`、`admin_users` 表
- 插入默认管理员账号（admin / admin123）

V2 脚本会：
- 创建 `users` 表（C端用户，邮箱登录）
- 创建 `favorites` 表（收藏，user_id + announcement_id 联合唯一）
- 创建 `application_records` 表（投递记录，状态流转）

### 启动

```bash
# 开发环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 编译打包
mvn clean package -DskipTests
java -jar target/campus-recruitment-backend-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

服务启动在 `http://localhost:8080/api`。

## API 概览

### C端接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /announcements | 公告列表（支持筛选/分页，登录后返回收藏/投递状态） |
| GET | /announcements/{id} | 公告详情（登录后返回收藏/投递状态） |
| GET | /announcements/filter-options | 筛选选项 |
| POST | /auth/send-magic-link | 发送 Magic Link 登录邮件（QQ 邮箱 SMTP） |
| GET | /auth/verify?token=xxx | 验证 Magic Link，获取 JWT |
| POST | /auth/logout | 退出登录 |
| GET | /auth/me | 获取当前用户信息 |
| POST | /favorites | 添加收藏 |
| DELETE | /favorites/{announcementId} | 取消收藏 |
| GET | /favorites | 我的收藏列表 |
| POST | /applications/toggle | 切换投递状态 |
| POST | /applications | 创建投递记录 |
| GET | /applications | 我的投递记录 |
| PUT | /applications/{id} | 更新投递状态/备注 |
| DELETE | /applications/{id} | 删除投递记录 |
| POST | /click-logs | 记录点击 |
| POST | /page-views | 记录访问 |

### 后台接口（需 JWT 认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /admin/login | 管理员登录 |
| GET | /admin/announcements | 公告管理列表 |
| POST | /admin/announcements | 新增公告 |
| PUT | /admin/announcements/{id} | 编辑公告 |
| DELETE | /admin/announcements/{id} | 删除公告（下线） |
| PATCH | /admin/announcements/{id}/status?onlineStatus=0/1 | 切换上下线 |
| GET | /admin/statistics | 仪表盘统计 |
| GET | /admin/statistics/top-companies | 热门企业TOP10 |

### 筛选参数

`GET /announcements` 支持以下可选查询参数：

- `page` - 页码（默认1）
- `pageSize` - 每页条数（默认20）
- `keyword` - 关键词搜索
- `classTypeIds` - 毕业届次（逗号分隔ID）
- `campusTypeIds` - 招聘批次（逗号分隔ID）
- `cityIds` - 城市（逗号分隔ID）
- `applyStatus` - 投递状态（ongoing/expired/not_started）

## 关键设计

- **双 JWT 体系**：管理端 JWT（role=ADMIN, 24h）+ 用户端 JWT（role=USER, 7天），统一由 JwtAuthenticationFilter 处理
- **Magic Link 登录**：邮箱发送（QQ 邮箱 SMTP，HTML 模板，@Async 异步） → Redis 存储 token（15min TTL）→ 验证后创建/查找用户 → 生成 JWT
- **频率限制**：同邮箱 1次/分钟 10次/天，同 IP 5次/分钟 50次/天，基于 Redis 计数器
- **投递状态流转**：applied → written_test → interview → offer/rejected，单向不可逆
- **公告列表用户态**：列表/详情接口通过 SecurityContext 获取当前用户，批量查询收藏和投递状态，避免 N+1
- **online_status**：独立于爬虫系统的 status 字段，控制前端可见性
- **申请状态**：实时计算（ONGOING/EXPIRED/NOT_STARTED），expired_at=NULL 时视为"招完即止"，排序用 published_at+90天
- **字段语义**：`from_url` = 投递链接，`link` = 宣发网址
- **动态筛选**：JPA Specification 实现，5个维度独立可选
- **埋点**：@Async 异步写入，不阻塞 API 响应
- **XSS 防护**：AntiSamy 清洗用户输入的 HTML 内容
