import request from '@/utils/request';
import type { PageResponse } from '@/types/api';
import type { AnnouncementListResponse, AnnouncementDetailResponse, FilterOptionsResponse, AnnouncementListParams } from '@/types/announcement';

export async function getAnnouncements(params: AnnouncementListParams) {
  const res = await request.get('/announcements', { params });
  return res.data as PageResponse<AnnouncementListResponse>;
}

export async function getAnnouncementDetail(id: number) {
  const res = await request.get(`/announcements/${id}`);
  return res.data as AnnouncementDetailResponse;
}

export async function getFilterOptions() {
  const res = await request.get('/announcements/filter-options');
  return res.data as FilterOptionsResponse;
}
