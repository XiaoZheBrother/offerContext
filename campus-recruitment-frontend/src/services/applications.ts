import request from '@/utils/request';
import type {
  ApplicationRecord,
  ApplicationToggleRequest,
  ApplicationCreateRequest,
  ApplicationUpdateRequest,
} from '@/types/application';

export async function toggleApplication(data: ApplicationToggleRequest): Promise<string> {
  return request.post('/applications/toggle', data);
}

export async function createApplication(data: ApplicationCreateRequest): Promise<ApplicationRecord> {
  return request.post('/applications', data);
}

export async function getApplications(): Promise<ApplicationRecord[]> {
  return request.get('/applications');
}

export async function updateApplication(id: number, data: ApplicationUpdateRequest): Promise<ApplicationRecord> {
  return request.put(`/applications/${id}`, data);
}

export async function deleteApplication(id: number): Promise<string> {
  return request.delete(`/applications/${id}`);
}
