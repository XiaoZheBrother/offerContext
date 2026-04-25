# OfferContext - 校招信息汇总平台

一站式校招信息聚合平台，从多渠道爬取校招公告，提供统一的浏览、筛选、收藏、投递追踪入口。

## 项目架构

```
offerContext/
├── campus-recruitment-backend/     # Spring Boot 后端
├── campus-recruitment-frontend/    # React 前端
└── docs/                          # 文档
    ├── deployment.md               # 部署文档
    ├── database-and-tech-design.md # 数据库与技术方案
    └── acceptance-checklist.md     # 验收清单
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | React 19 + TypeScript 6 + Ant Design 6 + Vite 8 + React Query 5 + Zustand 5 |
| 后端 | Java 17 + Spring Boot 3.2 + Spring Data JPA + Spring Security |
| 数据库 | MySQL 5.7 |
| 缓存 | Redis 6.0+（Magic Link Token + 频率限制） |
| 认证 | 双 JWT（管理端 + 用户端 Magic Link） |
| 安全 | AntiSamy XSS 防护 + Redis 频率限制 |

## 快速开始

### 前置条件

- JDK 17+
- Maven 3.3+
- Node.js 18+
- MySQL 5.7+
- Redis 6.0+

### 1. 初始化数据库

```bash
mysql -h <host> -u <user> -p <database> < campus-recruitment-backend/src/main/resources/db/migration/V1__create_new_tables.sql
mysql -h <host> -u <user> -p <database> < campus-recruitment-backend/src/main/resources/db/migration/V2__create_user_tables.sql
```

V2 迁移新增：`users`、`favorites`、`application_records` 表。

### 2. 启动后端

```bash
cd campus-recruitment-backend
# 修改 src/main/resources/application-dev.yml 中的数据库和 Redis 连接
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. 启动前端

```bash
cd campus-recruitment-frontend
npm install
npm run dev
```

### 4. 访问

- C端：http://localhost:5173
- 后台：http://localhost:5173/admin
- API：http://localhost:8080/api
- 默认管理员：admin / admin123
- C端用户：通过 Magic Link 邮箱登录（QQ 邮箱发送登录链接）

## 核心功能

### C端

- 校招公告列表：卡片式展示，按截止日期排序
- 5维筛选：关键词 / 毕业届次 / 招聘批次 / 城市 / 投递状态
- 公告详情：完整信息 + 一键投递
- 申请状态实时计算：进行中 / 已截止 / 未开始
- **用户登录**：邮箱 Magic Link 无密码登录
- **收藏**：一键收藏/取消，"我的收藏"页按截止时间排序高亮
- **投递记录**：标记投递 + 状态流转（已投递→笔试→面试→Offer/拒绝）
- **响应式适配**：桌面3列 / 平板2列 / 手机1列
- 埋点统计：PV/UV/点击

### 后台管理

- JWT 认证登录
- 仪表盘：在线公告数、今日 PV/UV、今日点击数
- 热门企业 TOP10（点击/浏览/投递 3维度）
- 公告 CRUD：新增（批次自动拆分+城市自定义）、编辑、删除、上下线切换

## 关键设计决策

| 决策 | 选择 | 原因 |
|------|------|------|
| 上下线控制 | 新增 online_status 列 | 爬虫系统 status=2 不可复用 |
| 申请状态 | 实时计算，不存储 | expired_at=NULL 视为"招完即止" |
| 默认过期时间 | published_at + 90天 | expired_at 为 NULL 时的排序兜底 |
| 投递入口 | from_url=投递链接, link=宣发网址 | 以实际数据字段语义为准 |
| 动态筛选 | JPA Specification | 5维度独立可选，避免组合爆炸 |
| 埋点写入 | @Async 异步 | 不阻塞 API 响应 |
| 用户认证 | 双 JWT（管理端 + 用户端） | C端与管理端独立认证，互不干扰 |
| Magic Link | Redis 存储 token（15min TTL）+ QQ 邮箱 SMTP 发送 | 一次性使用，自动过期，无需额外持久化 |
| 频率限制 | Redis 计数器 | 同邮箱 1次/分钟 10次/天，同 IP 5次/分钟 50次/天 |
| 投递状态流转 | applied → written_test → interview → offer/rejected | 单向流转，已进入后续阶段不可 toggle 取消 |
| 响应式 | CSS @media 查询 | 避免引入 Tailwind 与 Ant Design 样式冲突 |

## 版本历史

| 版本 | 日期 | 主要变更 |
|------|------|---------|
| 1.0 | 2026-04-24 | 公告浏览/筛选/详情 + 管理后台 CRUD + 埋点统计 |
| 2.0 | 2026-04-25 | 用户登录 + 收藏 + 投递记录 + 响应式适配 |

## 文档

- [部署文档](docs/deployment.md)
- [数据库与技术方案](docs/database-and-tech-design.md)
- [验收清单](docs/acceptance-checklist.md)
