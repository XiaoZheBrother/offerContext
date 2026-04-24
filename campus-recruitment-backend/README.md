# Campus Recruitment Backend

校招信息汇总平台后端服务，基于 Spring Boot 3.2 构建，提供校招公告浏览、筛选、后台管理等 API。

## 技术栈

- **Java 17** + **Spring Boot 3.2.0**
- **Spring Data JPA** + Hibernate（MySQL 5.7）
- **Spring Security** + JWT（jjwt 0.12.x）
- **AntiSamy** XSS 防护
- **Lombok**
- **Maven**

## 项目结构

```
src/main/java/com/campus/recruitment/
├── Application.java          # 启动类
├── common/                   # 通用类（ApiResponse, PageResponse）
├── config/                   # 配置类（SecurityConfig, JwtAuthenticationFilter）
├── controller/
│   ├── AnnouncementController.java   # C端：公告列表/详情/筛选
│   ├── TrackingController.java       # C端：点击/访问埋点
│   └── admin/                        # 后台管理
│       ├── AdminAuthController.java          # 登录
│       ├── AdminAnnouncementController.java  # 公告CRUD/上下线
│       └── AdminStatisticsController.java    # 统计数据
├── dto/
│   ├── ApplyStatus.java              # 申请状态枚举
│   ├── request/                      # 请求DTO
│   └── response/                     # 响应DTO
├── entity/                   # JPA实体（20+个）
├── exception/                # 异常处理
├── repository/               # JPA Repository
├── service/                  # 业务逻辑
└── util/                     # 工具类（XssUtils, UrlValidator, IpUtil）
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.3+
- MySQL 5.7+

### 配置

编辑 `src/main/resources/application-dev.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://<host>:<port>/<database>?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
    username: <username>
    password: <password>
```

JWT 密钥在 `application.yml` 中配置，生产环境务必通过环境变量覆盖。

### 数据库迁移

首次运行前，执行迁移脚本：

```bash
mysql -h <host> -u <user> -p <database> < src/main/resources/db/migration/V1__create_new_tables.sql
```

该脚本会：
- 为 `announcements` 表新增 `online_status` 列
- 更新 `expired_at` 为 NULL 的记录（默认 published_at + 90 天）
- 创建 `page_views`、`click_logs`、`admin_users` 表
- 插入默认管理员账号（admin / admin123）

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
| GET | /announcements | 公告列表（支持筛选/分页） |
| GET | /announcements/{id} | 公告详情 |
| GET | /announcements/filter-options | 筛选选项 |
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

- **online_status**：独立于爬虫系统的 status 字段，控制前端可见性
- **申请状态**：实时计算（ONGOING/EXPIRED/NOT_STARTED），expired_at=NULL 时视为"招完即止"，排序用 published_at+90天
- **字段语义**：`from_url` = 投递链接，`link` = 宣发网址
- **动态筛选**：JPA Specification 实现，5个维度独立可选
- **埋点**：@Async 异步写入，不阻塞 API 响应
- **XSS 防护**：AntiSamy 清洗用户输入的 HTML 内容
