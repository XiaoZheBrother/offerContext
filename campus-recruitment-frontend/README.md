# Campus Recruitment Frontend

校招信息汇总平台前端，基于 React 18 + TypeScript + Ant Design 构建，提供公告浏览筛选和后台管理功能。

## 技术栈

- **React 19** + **TypeScript 6**
- **Vite 8** 构建工具
- **Ant Design 6** UI 组件库
- **@tanstack/react-query 5** 数据请求与缓存
- **Zustand 5** 状态管理
- **React Router 7** 路由
- **Axios** HTTP 客户端
- **Day.js** 日期处理
- **js-cookie** Cookie 管理

## 项目结构

```
src/
├── components/       # 公共组件（FilterBar, AnnouncementCard 等）
├── hooks/            # React Query hooks
├── pages/            # 页面组件
│   └── admin/        # 后台管理页面
├── services/         # API 调用封装
├── store/            # Zustand 状态管理
├── styles/           # 全局样式
├── types/            # TypeScript 类型定义
└── utils/            # 工具函数
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

启动在 `http://localhost:3000`，API 请求自动代理到 `http://localhost:8080/api`。

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

- 公告列表：卡片网格布局（2-3列响应式）
- 多维筛选：毕业年份 / 招聘批次 / 城市 / 投递状态 / 关键词搜索
- 公告详情：完整信息展示 + 投递按钮
- 筛选状态持久化：URL searchParams 驱动，支持分享/收藏

### 后台

- 管理员登录（JWT 认证）
- 仪表盘统计：在线公告数、今日 PV/UV、今日点击
- 热门企业 TOP10（3维度：点击/浏览/投递）
- 公告管理：CRUD + 上下线开关
- 新增公告：批次自动拆分 + 城市自定义

## 配置

`vite.config.ts` 中配置了：

- 路径别名：`@` → `src/`
- 开发服务器端口：3000
- API 代理：`/api` → `http://localhost:8080`

生产环境部署时需配置 Nginx 反向代理将 `/api` 请求转发到后端服务。
