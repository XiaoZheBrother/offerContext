export const ROUTES = {
  HOME: '/',
  ANNOUNCEMENTS: '/announcements',
  ANNOUNCEMENT_DETAIL: '/announcements/:id',
  ADMIN_LOGIN: '/admin/login',
  ADMIN: '/admin',
  ADMIN_ANNOUNCEMENTS: '/admin/announcements',
} as const;

export const APPLY_STATUS_LABELS: Record<string, string> = {
  all: '全部',
  ongoing: '进行中',
  expired: '已截止',
  not_started: '未开始',
};

export const APPLY_STATUS_COLORS: Record<string, string> = {
  ongoing: 'green',
  expired: 'red',
  not_started: 'orange',
};

export const CLICK_TYPE = {
  LINK: 'link',
  EMAIL: 'email',
} as const;

export const MIN_PC_WIDTH = 1024;
export const DEFAULT_PAGE_SIZE = 20;
