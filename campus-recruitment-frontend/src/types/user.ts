export interface User {
  id: number;
  email: string;
  nickname: string;
  avatarUrl: string;
}

export interface SendMagicLinkRequest {
  email: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface SendMagicLinkResponse {
  message: string;
  token: string;
}

export const APPLICATION_STATUS = {
  APPLIED: 'APPLIED',
  WRITTEN_TEST: 'WRITTEN_TEST',
  INTERVIEW: 'INTERVIEW',
  OFFER: 'OFFER',
  REJECTED: 'REJECTED',
} as const;

export type ApplicationStatusType = (typeof APPLICATION_STATUS)[keyof typeof APPLICATION_STATUS];

export const APPLICATION_STATUS_LABELS: Record<ApplicationStatusType, string> = {
  APPLIED: '已投递',
  WRITTEN_TEST: '笔试',
  INTERVIEW: '面试',
  OFFER: '已获offer',
  REJECTED: '已拒绝',
};

export const APPLICATION_STATUS_COLORS: Record<ApplicationStatusType, string> = {
  APPLIED: '#52c41a',
  WRITTEN_TEST: '#1890ff',
  INTERVIEW: '#722ed1',
  OFFER: '#faad14',
  REJECTED: '#ff4d4f',
};
