# OfferContext - 校招信息汇总平台

一站式校招信息聚合平台，从多渠道爬取校招公告，提供统一的浏览、筛选、投递入口。

## 项目架构

```
offerContext/
├── campus-recruitment-backend/     # Spring Boot 后端
├── campus-recruitment-frontend/    # React 前端
└── docs/                          # 文档
    └── deployment.md              # 部署文档
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | React 19 + TypeScript 6 + Ant Design 6 + Vite 8 |
| 后端 | Java 17 + Spring Boot 3.2 + Spring Data JPA + Spring Security |
| 数据库 | MySQL 5.7 |
| 认证 | JWT (jjwt 0.12.x) |
| 安全 | AntiSamy XSS 防护 |

## 快速开始

### 前置条件

- JDK 17+
- Maven 3.3+
- Node.js 18+
- MySQL 5.7+

### 1. 初始化数据库

```bash
mysql -h <host> -u <user> -p <database> < campus-recruitment-backend/src/main/resources/db/migration/V1__create_new_tables.sql
```

### 2. 启动后端

```bash
cd campus-recruitment-backend
# 修改 src/main/resources/application-dev.yml 中的数据库连接
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. 启动前端

```bash
cd campus-recruitment-frontend
npm install
npm run dev
```

### 4. 访问

- C端：http://localhost:3000
- 后台：http://localhost:3000/admin
- API：http://localhost:8080/api
- 默认管理员：admin / admin123

## 核心功能

### C端

- 校招公告列表：卡片式展示，按截止日期排序
- 5维筛选：关键词 / 毕业届次 / 招聘批次 / 城市 / 投递状态
- 公告详情：完整信息 + 一键投递
- 申请状态实时计算：进行中 / 已截止 / 未开始
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

## 子项目

- [后端文档](campus-recruitment-backend/README.md)
- [前端文档](campus-recruitment-frontend/README.md)
- [部署文档](docs/deployment.md)
