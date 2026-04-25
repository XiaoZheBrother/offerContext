import type { ApplicationStatusType } from './user';

export interface ApplicationRecord {
  id: number;
  announcementId: number;
  companyName: string;
  announcementName: string;
  status: ApplicationStatusType;
  notes: string | null;
  appliedAt: string;
  updatedAt: string;
}

export interface ApplicationToggleRequest {
  announcementId: number;
}

export interface ApplicationCreateRequest {
  announcementId: number;
  status?: ApplicationStatusType;
  notes?: string;
}

export interface ApplicationUpdateRequest {
  status?: ApplicationStatusType;
  notes?: string;
}
