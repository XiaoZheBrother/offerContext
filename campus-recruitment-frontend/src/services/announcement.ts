import request from '@/utils/request';
import type { ApiResponse, PageResponse } from '@/types/api';
import type {
  AnnouncementListResponse,
  AnnouncementDetailResponse,
  FilterOptionsResponse,
  AnnouncementListParams,
} from '@/types/announcement';

export async function getAnnouncements(params: AnnouncementListParams) {
  const res = await request.get<ApiResponse<PageResponse<AnnouncementListResponse>>>('/announcements', { params });
  return res.data;
}

export async function getAnnouncementDetail(id: number) {
  const res = await request.get<ApiResponse<AnnouncementDetailResponse>>(`/announcements/${id}`);
  return res.data;
}

export async function getFilterOptions() {
  const res = await request.get<ApiResponse<FilterOptionsResponse>>('/announcements/filter-options');
  return res.data;
}
