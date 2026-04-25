import request from '@/utils/request';
import type { SendMagicLinkRequest, SendMagicLinkResponse, AuthResponse, User } from '@/types/user';

export async function sendMagicLink(data: SendMagicLinkRequest): Promise<SendMagicLinkResponse> {
  return request.post('/auth/send-magic-link', data);
}

export async function verifyMagicLink(token: string): Promise<AuthResponse> {
  return request.get('/auth/verify', { params: { token } });
}

export async function logout(): Promise<void> {
  return request.post('/auth/logout');
}

export async function getMe(): Promise<User> {
  return request.get('/auth/me');
}
