import request from '@/utils/request';
import type { PageResponse } from '@/types/api';
import type { AnnouncementListResponse, AnnouncementCreateRequest } from '@/types/announcement';
import type { LoginResponse, StatisticsResponse, TopCompanyItem, AdminAnnouncementListParams } from '@/types/admin';

export async function login(username: string, password: string) {
  const res = await request.post('/admin/login', { username, password });
  return res.data as LoginResponse;
}

export async function getAdminAnnouncements(params: AdminAnnouncementListParams) {
  const res = await request.get('/admin/announcements', { params });
  return res.data as PageResponse<AnnouncementListResponse>;
}

export async function createAnnouncement(data: AnnouncementCreateRequest) {
  const res = await request.post('/admin/announcements', data);
  return res.data as number;
}

export async function updateAnnouncement(id: number, data: AnnouncementCreateRequest) {
  await request.put(`/admin/announcements/${id}`, data);
}

export async function deleteAnnouncement(id: number) {
  await request.delete(`/admin/announcements/${id}`);
}

export async function updateAnnouncementStatus(id: number, onlineStatus: number) {
  await request.patch(`/admin/announcements/${id}/status`, null, {
    params: { onlineStatus },
  });
}

export async function getStatistics() {
  const res = await request.get('/admin/statistics');
  return res.data as StatisticsResponse;
}

export async function getTopCompanies(dimension: string, limit = 10) {
  const res = await request.get('/admin/statistics/top-companies', {
    params: { dimension, limit },
  });
  return res.data as TopCompanyItem[];
}
