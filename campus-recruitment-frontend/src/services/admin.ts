import request from '@/utils/request';
import type { ApiResponse, PageResponse } from '@/types/api';
import type { AnnouncementListResponse, AnnouncementCreateRequest } from '@/types/announcement';
import type { LoginRequest, LoginResponse, StatisticsResponse, TopCompanyItem, AdminAnnouncementListParams } from '@/types/admin';

export async function login(data: LoginRequest) {
  const res = await request.post<ApiResponse<LoginResponse>>('/admin/login', data);
  return res.data;
}

export async function getAdminAnnouncements(params: AdminAnnouncementListParams) {
  const res = await request.get<ApiResponse<PageResponse<AnnouncementListResponse>>>('/admin/announcements', { params });
  return res.data;
}

export async function createAnnouncement(data: AnnouncementCreateRequest) {
  const res = await request.post<ApiResponse<number>>('/admin/announcements', data);
  return res.data;
}

export async function updateAnnouncement(id: number, data: AnnouncementCreateRequest) {
  await request.put<ApiResponse<void>>(`/admin/announcements/${id}`, data);
}

export async function deleteAnnouncement(id: number) {
  await request.delete<ApiResponse<void>>(`/admin/announcements/${id}`);
}

export async function updateAnnouncementStatus(id: number, onlineStatus: number) {
  await request.patch<ApiResponse<void>>(`/admin/announcements/${id}/status`, null, {
    params: { onlineStatus },
  });
}

export async function getStatistics() {
  const res = await request.get<ApiResponse<StatisticsResponse>>('/admin/statistics');
  return res.data;
}

export async function getTopCompanies(dimension: string, limit = 10) {
  const res = await request.get<ApiResponse<TopCompanyItem[]>>('/admin/statistics/top-companies', {
    params: { dimension, limit },
  });
  return res.data;
}
