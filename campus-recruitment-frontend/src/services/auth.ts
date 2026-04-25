import request from '@/utils/request';
import type { SendMagicLinkRequest, SendMagicLinkResponse, AuthResponse, User } from '@/types/user';

export async function sendMagicLink(data: SendMagicLinkRequest): Promise<SendMagicLinkResponse> {
  const res = await request.post('/auth/send-magic-link', data);
  return res.data as SendMagicLinkResponse;
}

export async function verifyMagicLink(token: string): Promise<AuthResponse> {
  const res = await request.get('/auth/verify', { params: { token } });
  return res.data as AuthResponse;
}

export async function logout(): Promise<void> {
  await request.post('/auth/logout');
}

export async function getMe(): Promise<User> {
  const res = await request.get('/auth/me');
  return res.data as User;
}
