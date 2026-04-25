import request from '@/utils/request';
import type {
  ApplicationRecord,
  ApplicationToggleRequest,
  ApplicationCreateRequest,
  ApplicationUpdateRequest,
} from '@/types/application';

export async function toggleApplication(data: ApplicationToggleRequest): Promise<string> {
  const res = await request.post('/applications/toggle', data);
  return res.data;
}

export async function createApplication(data: ApplicationCreateRequest): Promise<ApplicationRecord> {
  const res = await request.post('/applications', data);
  return res.data as ApplicationRecord;
}

export async function getApplications(): Promise<ApplicationRecord[]> {
  const res = await request.get('/applications');
  return res.data as ApplicationRecord[];
}

export async function updateApplication(id: number, data: ApplicationUpdateRequest): Promise<ApplicationRecord> {
  const res = await request.put(`/applications/${id}`, data);
  return res.data as ApplicationRecord;
}

export async function deleteApplication(id: number): Promise<string> {
  const res = await request.delete(`/applications/${id}`);
  return res.data;
}
