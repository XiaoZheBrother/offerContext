export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
}

export interface TopCompanyItem {
  companyName: string;
  clickCount: number;
}

export interface StatisticsResponse {
  onlineCount: number;
  todayPv: number;
  todayUv: number;
  todayClickCount: number;
  topCompanies: TopCompanyItem[];
}

export interface AdminAnnouncementListParams {
  page?: number;
  size?: number;
  keyword?: string;
  status?: string;
}
