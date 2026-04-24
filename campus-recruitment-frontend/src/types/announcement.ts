import type { ApplyStatus } from './api';

export interface AnnouncementListResponse {
  announcementId: number;
  companyName: string;
  name: string;
  classTypeNames: string[];
  campusTypeNames: string[];
  cityNames: string[];
  expiredAt: string;
  applyStatus: ApplyStatus;
  createdAt: string;
}

export interface AnnouncementDetailResponse {
  announcementId: number;
  companyName: string;
  name: string;
  detail: string;
  fromUrl: string;
  link: string;
  classTypeNames: string[];
  campusTypeNames: string[];
  cityNames: string[];
  degreeNames: string[];
  industryTypeNames: string[];
  jobCategoryNames: string[];
  publishedAt: string;
  expiredAt: string;
  applyStatus: ApplyStatus;
  createdAt: string;
  salary: string;
  companyWelfare: string;
  classTime: string;
  writtenTest: boolean;
  acceptWorkExperience: boolean;
  companyDescriptions: string[];
  companyIndustryNames: string[];
}

export interface FilterItem {
  id: number;
  name: string;
  isTop?: boolean;
}

export interface FilterOptionsResponse {
  classTypes: FilterItem[];
  campusTypes: FilterItem[];
  cities: FilterItem[];
}

export interface AnnouncementListParams {
  keyword?: string;
  classTypeIds?: number[];
  campusTypeIds?: number[];
  cityIds?: number[];
  applyStatus?: string;
  page?: number;
  pageSize?: number;
}

export interface AnnouncementCreateRequest {
  companyName: string;
  name: string;
  detail?: string;
  link?: string;
  classTypeIds: number[];
  campusTypeIds: number[];
  cityIds: number[];
  customCities?: string[];
  publishedAt: string;
  expiredAt: string;
  applyLink: string;
  onlineStatus?: number;
  createdAt?: string;
}
