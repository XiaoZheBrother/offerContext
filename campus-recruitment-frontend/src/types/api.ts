export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResponse<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

export const APPLY_STATUS = {
  ALL: 'all',
  ONGOING: 'ongoing',
  EXPIRED: 'expired',
  NOT_STARTED: 'not_started',
} as const;

export type ApplyStatus = (typeof APPLY_STATUS)[keyof typeof APPLY_STATUS];
