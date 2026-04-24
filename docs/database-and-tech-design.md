# 校招信息汇总网站 MVP 1.0 数据库设计与技术方案（实现版）

> 版本：V1.0 | 状态：已实现 | 日期：2026-04-24 | 基于 PRD V1.1

## 1. 技术栈

后端：Spring Boot 3.2.0 / JDK 17 / Spring Data JPA + Hibernate 6.4.x / Spring Security + JWT (jjwt 0.12.3) / AntiSamy 1.7.5 / MySQL 5.7

前端：React 19 / TypeScript 6 / Vite 8 / Ant Design 6 / @tanstack/react-query 5 / Zustand 5 / react-router-dom 7 / Axios 1.15.x / Day.js 1.11.x

## 2. 项目架构

Monorepo 结构：campus-recruitment-backend（Spring Boot）+ campus-recruitment-frontend（React）+ docs/

后端包结构：common（ApiResponse/PageResponse）、config（SecurityConfig/JwtAuthenticationFilter）、controller（C端+admin）、dto（11个）、entity（19个）、exception、repository（19个）、service（7个）、util（XssUtils/UrlValidator/IpUtil）

前端目录：components（FilterBar/AnnouncementCard/AdminRoute/MobileGuard）、hooks（useFilter/useWindowWidth）、pages（AnnouncementList/AnnouncementDetail/admin/Login/Dashboard/AnnouncementManage）、services、store（auth Zustand）、types、utils（request/constants）

## 3. 数据库设计

数据库：cb-crawl-dev（与爬虫系统共享），utf8mb4，现有数据约 13,750 条公告、8,230 家公司、625 个城市。核心表 8 张、关联表 6 张、辅助表 2 张、新增表 3 张。

ER关系：companies 1:M announcements（via company_id）；announcements M:M cities/class_types/campus_types/degrees/industry_types/job_categories（via 6张关联表）；companies M:M industry_types（via company_industry_types）；companies 1:M company_descriptions；click_logs M:1 announcements + companies；page_views 和 admin_users 为独立表。

announcements 核心字段：announcement_id(INT PK)、name(VARCHAR 255)、detail(TEXT)、salary(VARCHAR 255)、company_id(INT FK)、link(TEXT, 宣发网址)、from_url(TEXT, 投递链接)、published_at(DATETIME, 发布/网申开始日期)、expired_at(DATETIME, 截止日期, NULL视为招完即止)、online_status(TINYINT, 0=下线1=上线, V1迁移新增默认1)、status(TINYINT, 爬虫状态全为2不用于上下线)、class_time(VARCHAR 255)、written_test(TINYINT)、company_welfare(TEXT)、accept_work_experience(TINYINT)、created_at/updated_at(DATETIME)

companies 字段：company_id(INT PK)、name(VARCHAR 255)、created_at/updated_at(DATETIME)

6张维度表（cities/class_types/campus_types/degrees/industry_types/job_categories）统一结构：{table}_id(INT PK)、name(VARCHAR 255)、description(TEXT)、created_at/updated_at。cities额外：initial(VARCHAR 10)、is_top(TINYINT)、weight(INT)、code(INT)。degrees额外：level(INT)。

6张关联表统一结构：id(INT PK AUTO_INCREMENT)、announcement_id(INT FK)、{dimension}_id(INT FK)、created_at/updated_at。

page_views（V1新增）：id(BIGINT PK AUTO_INCREMENT)、visitor_id(VARCHAR 255)、page_url(TEXT)、page_type(VARCHAR 100, list/detail)、referer(TEXT)、user_agent(TEXT)、ip_address(VARCHAR 45)、visit_time(DATETIME)、created_at(DATETIME)。索引：idx_pv_visitor_time, idx_pv_page_type_time, idx_pv_visit_time

click_logs（V1新增）：id(BIGINT PK AUTO_INCREMENT)、announcement_id(INT)、company_id(INT)、visitor_id(VARCHAR 255)、click_type(VARCHAR 50, link/email)、ip_address(VARCHAR 45)、click_time(DATETIME)、created_at(DATETIME)。索引：idx_cl_announcement_time, idx_cl_company_time, idx_cl_click_time

admin_users（V1新增）：id(INT PK AUTO_INCREMENT)、username(VARCHAR 100 UNIQUE)、password(VARCHAR 255, BCrypt)、email(VARCHAR 255)、status(TINYINT, 1=启用0=禁用)、last_login_at(DATETIME)、created_at/updated_at(DATETIME)

## 4. 后端架构设计

分层：Controller（参数校验@Valid + ApiResponse包装）→ Service（业务逻辑 + AntiSamy XSS清洗）→ Repository（JPA Specification动态查询 + 原生SQL统计）→ Entity/DB

安全：JWT HMAC-SHA384 24h过期；C端全放行；/admin/login放行，其余/admin/**需JWT；AntiSamy富文本清洗；CORS允许localhost:3000/5173；URL校验禁止javascript:/vbscript:/data:协议

C端API：GET /announcements（5维筛选+分页）、GET /announcements/{id}（详情）、GET /announcements/filter-options（筛选选项）、POST /click-logs（点击埋点）、POST /page-views（访问埋点）

后台API：POST /admin/login、GET /admin/announcements、POST /admin/announcements、PUT /admin/announcements/{id}、DELETE /admin/announcements/{id}、PATCH /admin/announcements/{id}/status、GET /admin/statistics、GET /admin/statistics/top-companies

关键技术决策：online_status新增列（爬虫status不可复用）、申请状态实时计算（expired_at=NULL视为招完即止）、默认过期时间published_at+90天、from_url=投递链接link=宣发网址、JPA Specification动态筛选、@Async埋点写入、ConcurrentHashMap+1hTTL筛选缓存、多批次选择时自动拆分多条记录

## 5. 前端架构设计

数据流：URL为真相源（筛选/分页存searchParams）、React Query管理服务端数据、Zustand仅管认证（token+Cookies 7天持久化）、表单状态本地管理

路由：/announcements（列表首页无需认证）、/announcements/:id（详情无需认证）、/admin/login（登录无需认证）、/admin（仪表盘需JWT）、/admin/announcements（公告管理需JWT）

前端关键设计：筛选需点"应用筛选"按钮才生效、CSS Grid 3列/2列响应、小于1024px显示"请使用电脑访问"、投递按钮自动识别链接/邮箱、骨架屏首次加载、空状态中文提示、antd ConfigProvider zh_CN + dayjs zh-cn

## 6. 部署架构

Nginx(80/443) → 前端静态文件(dist/) + /api/反向代理→Spring Boot(8080) + /assets/静态资源缓存30d

前端：Vite build → dist/ 部署到Nginx
后端：mvn package → .jar，Systemd服务管理
数据库：远程MySQL 5.7，HikariCP连接池最大5连接
生产环境：JWT密钥/数据库密码通过环境变量注入
