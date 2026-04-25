# Campus Recruitment Frontend

校招信息汇总平台前端，基于 React 19 + TypeScript + Ant Design 构建，提供公告浏览筛选、用户登录、收藏、投递记录和后台管理功能。

## 技术栈

- **React 19** + **TypeScript 6**
- **Vite 8** 构建工具
- **Ant Design 6** UI 组件库
- **@tanstack/react-query 5** 数据请求与缓存
- **Zustand 5** 状态管理
- **React Router 7** 路由
- **Axios** HTTP 客户端
- **Day.js** 日期处理
- **js-cookie** Cookie 管理（管理端 token）

## 项目结构

```
src/
├── components/       # 公共组件
│   ├── FilterBar/          # 筛选栏
│   ├── AnnouncementCard/   # 公告卡片（含收藏/投递图标）
│   ├── LoginModal/         # Magic Link 登录弹窗
│   ├── UserMenu/           # 已登录用户下拉菜单
│   ├── RecordApplicationModal/ # 投递记录弹窗
│   ├── SiteHeader/         # 顶部导航栏
│   └── AdminLayout/        # 后台布局
├── hooks/            # React Query hooks + useFilter
├── pages/            # 页面组件
│   ├── AnnouncementList/   # 公告列表（响应式 3/2/1 列）
│   ├── AnnouncementDetail/ # 公告详情（含收藏/投递）
│   ├── Favorites.tsx       # 我的收藏
│   ├── Applications.tsx    # 投递记录
│   └── admin/              # 后台管理页面
├── services/         # API 调用封装（auth, favorites, applications, announcement, tracking）
├── store/            # Zustand 状态管理（auth 管理端, userAuthStore 用户端）
├── styles/           # 全局样式 + CSS 变量
├── types/            # TypeScript 类型定义（announcement, user, favorite, application）
└── utils/            # 工具函数（request 双 token, constants）
```

## 快速开始

### 环境要求

- Node.js 18+
- npm 9+

### 安装依赖

```bash
cd campus-recruitment-frontend
npm install
```

### 开发

```bash
npm run dev
```

启动在 `http://localhost:5173`，API 请求自动代理到 `http://localhost:8080/api`。

### 构建

```bash
npm run build
```

产出目录 `dist/`，可部署到 Nginx 等静态服务器。

### 预览构建产物

```bash
npm run preview
```

## 核心功能

### C端

- 公告列表：卡片网格布局（桌面3列/平板2列/手机1列响应式）
- 多维筛选：毕业年份 / 招聘批次 / 城市 / 投递状态 / 关键词搜索
- 公告详情：完整信息展示 + 投递按钮 + 收藏按钮
- **用户登录**：Magic Link 邮箱无密码登录（开发模式直接返回 token）
- **收藏**：卡片/详情页一键收藏，"我的收藏"页按截止时间排序高亮
- **投递记录**：卡片投递开关 + 详情页投递记录弹窗 + 投递记录管理页
- 筛选状态持久化：URL searchParams 驱动，支持分享/收藏
- 双 token 认证：用户 token（localStorage）优先，管理端 token（Cookie）备选

### 后台

- 管理员登录（JWT 认证）
- 仪表盘统计：在线公告数、今日 PV/UV、今日点击
- 热门企业 TOP10（3维度：点击/浏览/投递）
- 公告管理：CRUD + 上下线开关
- 新增公告：批次自动拆分 + 城市自定义

## 配置

`vite.config.ts` 中配置了：

- 路径别名：`@` → `src/`
- 开发服务器端口：5173
- API 代理：`/api` → `http://localhost:8080`

生产环境部署时需配置 Nginx 反向代理将 `/api` 请求转发到后端服务。
