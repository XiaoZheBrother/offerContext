# 校招信息汇总网站 数据库设计与技术方案（实现版）

> 版本：V2.0 | 状态：已实现 | 日期：2026-04-25 | 基于 PRD V2.0

## 1. 技术栈

后端：Spring Boot 3.2.0 / JDK 17 / Spring Data JPA + Hibernate 6.4.x / Spring Security + 双JWT (jjwt 0.12.3) / Spring Data Redis / AntiSamy 1.7.5 / MySQL 5.7 / Redis 6.0+

前端：React 19 / TypeScript 6 / Vite 8 / Ant Design 6 / @tanstack/react-query 5 / Zustand 5 / react-router-dom 7 / Axios 1.15.x / Day.js 1.11.x

## 2. 项目架构

Monorepo 结构：campus-recruitment-backend（Spring Boot）+ campus-recruitment-frontend（React）+ docs/

后端包结构：common（ApiResponse/PageResponse）、config（SecurityConfig/JwtAuthenticationFilter/RateLimitFilter）、controller（C端+admin）、dto（19个）、entity（22个，含User/Favorite/ApplicationRecord）、enums（ApplicationStatus）、exception、repository（22个）、service（11个，含UserAuthService/RateLimitService/FavoriteService/ApplicationService）、util（XssUtils/UrlValidator/IpUtil）

前端目录：components（FilterBar/AnnouncementCard/LoginModal/UserMenu/RecordApplicationModal/SiteHeader/AdminLayout）、hooks（useFilter/useWindowWidth）、pages（AnnouncementList/AnnouncementDetail/Favorites/Applications/admin/Login/Dashboard/AnnouncementManage）、services（auth/favorites/applications/announcement/tracking）、store（auth Zustand管理端/userAuthStore Zustand用户端）、types（announcement/user/favorite/application）、utils（request/constants）

## 3. 数据库设计

数据库：cb-crawl-dev（与爬虫系统共享），utf8mb4，现有数据约 13,750 条公告、8,230 家公司、625 个城市。核心表 8 张、关联表 6 张、辅助表 2 张、1.0新增表 3 张、2.0新增表 3 张。

ER关系：companies 1:M announcements（via company_id）；announcements M:M cities/class_types/campus_types/degrees/industry_types/job_categories（via 6张关联表）；companies M:M industry_types（via company_industry_types）；companies 1:M company_descriptions；click_logs M:1 announcements + companies；page_views 和 admin_users 为独立表；users 1:M favorites（via user_id, CASCADE DELETE）；users 1:M application_records（via user_id, CASCADE DELETE）；favorites M:1 announcements（via announcement_id, CASCADE DELETE）；application_records M:1 announcements（via announcement_id, CASCADE DELETE）。

announcements 核心字段：announcement_id(INT PK)、name(VARCHAR 255)、detail(TEXT)、salary(VARCHAR 255)、company_id(INT FK)、link(TEXT, 宣发网址)、from_url(TEXT, 投递链接)、published_at(DATETIME, 发布/网申开始日期)、expired_at(DATETIME, 截止日期, NULL视为招完即止)、online_status(TINYINT, 0=下线1=上线, V1迁移新增默认1)、status(TINYINT, 爬虫状态全为2不用于上下线)、class_time(VARCHAR 255)、written_test(TINYINT)、company_welfare(TEXT)、accept_work_experience(TINYINT)、created_at/updated_at(DATETIME)

companies 字段：company_id(INT PK)、name(VARCHAR 255)、created_at/updated_at(DATETIME)

6张维度表（cities/class_types/campus_types/degrees/industry_types/job_categories）统一结构：{table}_id(INT PK)、name(VARCHAR 255)、description(TEXT)、created_at/updated_at。cities额外：initial(VARCHAR 10)、is_top(TINYINT)、weight(INT)、code(INT)。degrees额外：level(INT)。

6张关联表统一结构：id(INT PK AUTO_INCREMENT)、announcement_id(INT FK)、{dimension}_id(INT FK)、created_at/updated_at。

page_views（V1新增）：id(BIGINT PK AUTO_INCREMENT)、visitor_id(VARCHAR 255)、page_url(TEXT)、page_type(VARCHAR 100, list/detail)、referer(TEXT)、user_agent(TEXT)、ip_address(VARCHAR 45)、visit_time(DATETIME)、created_at(DATETIME)。索引：idx_pv_visitor_time, idx_pv_page_type_time, idx_pv_visit_time

click_logs（V1新增）：id(BIGINT PK AUTO_INCREMENT)、announcement_id(INT)、company_id(INT)、visitor_id(VARCHAR 255)、click_type(VARCHAR 50, link/email)、ip_address(VARCHAR 45)、click_time(DATETIME)、created_at(DATETIME)。索引：idx_cl_announcement_time, idx_cl_company_time, idx_cl_click_time

admin_users（V1新增）：id(INT PK AUTO_INCREMENT)、username(VARCHAR 100 UNIQUE)、password(VARCHAR 255, BCrypt)、email(VARCHAR 255)、status(TINYINT, 1=启用0=禁用)、last_login_at(DATETIME)、created_at/updated_at(DATETIME)

users（V2新增）：id(BIGINT PK AUTO_INCREMENT)、email(VARCHAR 255 UNIQUE NOT NULL)、nickname(VARCHAR 50 DEFAULT '用户')、avatar_url(VARCHAR 500 DEFAULT '/default-avatar.png')、created_at(TIMESTAMP DEFAULT CURRENT_TIMESTAMP)、last_login_at(TIMESTAMP)。INDEX idx_email (email)。首次登录自动创建，nickname 默认"用户" + id后4位。

favorites（V2新增）：id(BIGINT PK AUTO_INCREMENT)、user_id(BIGINT NOT NULL)、announcement_id(INT NOT NULL)、created_at(TIMESTAMP DEFAULT CURRENT_TIMESTAMP)。UNIQUE KEY uk_user_announcement (user_id, announcement_id)。INDEX idx_user_id (user_id)。FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE。FOREIGN KEY (announcement_id) REFERENCES announcements(announcement_id) ON DELETE CASCADE。

application_records（V2新增）：id(BIGINT PK AUTO_INCREMENT)、user_id(BIGINT NOT NULL)、announcement_id(INT NOT NULL)、status(VARCHAR(20) DEFAULT 'applied', 枚举值: APPLIED/WRITTEN_TEST/INTERVIEW/OFFER/REJECTED)、notes(TEXT)、applied_at(TIMESTAMP DEFAULT CURRENT_TIMESTAMP)、updated_at(TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)。UNIQUE KEY uk_user_announcement (user_id, announcement_id)。INDEX idx_user_id (user_id)。INDEX idx_status (status)。FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE。FOREIGN KEY (announcement_id) REFERENCES announcements(announcement_id) ON DELETE CASCADE。

## 4. 后端架构设计

分层：Controller（参数校验@Valid + ApiResponse包装）→ Service（业务逻辑 + AntiSamy XSS清洗）→ Repository（JPA Specification动态查询 + 原生SQL统计）→ Entity/DB

安全：双JWT体系（管理端JWT HMAC-SHA384 24h过期 + 用户端JWT 7天过期）；/auth/**放行；/favorites/**和/applications/**需USER角色；/admin/**需ADMIN角色；AntiSamy富文本清洗；CORS允许localhost:3000/5173；URL校验禁止javascript:/vbscript:/data:协议；Redis频率限制

C端API：GET /announcements（5维筛选+分页，登录后返回isFavorited/isApplied/applicationStatus）、GET /announcements/{id}（详情，登录后返回收藏/投递状态）、GET /announcements/filter-options（筛选选项）、POST /auth/send-magic-link（发送Magic Link，开发模式直接返回token）、GET /auth/verify?token=xxx（验证Magic Link获取JWT）、POST /auth/logout、GET /auth/me、POST /favorites、DELETE /favorites/{announcementId}、GET /favorites、POST /applications/toggle、POST /applications、GET /applications、PUT /applications/{id}、DELETE /applications/{id}、POST /click-logs（点击埋点）、POST /page-views（访问埋点）

后台API：POST /admin/login、GET /admin/announcements、POST /admin/announcements、PUT /admin/announcements/{id}、DELETE /admin/announcements/{id}、PATCH /admin/announcements/{id}/status、GET /admin/statistics、GET /admin/statistics/top-companies

关键技术决策：online_status新增列（爬虫status不可复用）、申请状态实时计算（expired_at=NULL视为招完即止）、默认过期时间published_at+90天、from_url=投递链接link=宣发网址、JPA Specification动态筛选、@Async埋点写入、ConcurrentHashMap+1hTTL筛选缓存、多批次选择时自动拆分多条记录、双JWT体系（管理端+用户端独立认证）、Magic Link登录（Redis存储token 15min一次性）、Redis频率限制、投递状态单向流转、公告列表批量查询用户态字段避免N+1

## 5. 前端架构设计

数据流：URL为真相源（筛选/分页存searchParams）、React Query管理服务端数据、Zustand管双认证（管理端auth token+Cookies 7天持久化，用户端userAuthStore token+localStorage 7天持久化）、表单状态本地管理

路由：/announcements（列表首页无需认证）、/announcements/:id（详情无需认证）、/favorites（收藏页，未登录弹登录窗）、/applications（投递记录页，未登录弹登录窗）、/admin/login（登录无需认证）、/admin（仪表盘需管理端JWT）、/admin/announcements（公告管理需管理端JWT）

前端关键设计：筛选需点"应用筛选"按钮才生效、CSS Grid 3/2/1列响应式、移动端核心路径全尺寸可用、投递按钮自动识别链接/邮箱、骨架屏首次加载、空状态中文提示、antd ConfigProvider zh_CN + dayjs zh-cn、双token请求拦截（用户token优先于管理端token）、401自动区分管理端/用户端清理

## 6. 部署架构

Nginx(80/443) → 前端静态文件(dist/) + /api/反向代理→Spring Boot(8080) + /assets/静态资源缓存30d

前端：Vite build → dist/ 部署到Nginx
后端：mvn package → .jar，Systemd服务管理
数据库：远程MySQL 5.7，HikariCP连接池最大5连接
生产环境：JWT密钥（管理端+用户端）/数据库密码/Redis配置通过环境变量注入
