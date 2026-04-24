import axios from 'axios';
import { message } from 'antd';
import { useAuthStore } from '@/store/auth';

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  paramsSerializer: {
    serialize: (params: Record<string, unknown>) => {
      const parts: string[] = [];
      for (const [key, value] of Object.entries(params)) {
        if (value == null || value === '') continue;
        if (Array.isArray(value)) {
          value.forEach((v) => parts.push(`${key}=${encodeURIComponent(String(v))}`));
        } else {
          parts.push(`${key}=${encodeURIComponent(String(value))}`);
        }
      }
      return parts.join('&');
    },
  },
});

request.interceptors.request.use((config) => {
  const { token } = useAuthStore.getState();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().clearAuth();
      window.location.href = '/admin/login';
    } else {
      const msg = error.response?.data?.message || '请求失败，请稍后重试';
      message.error(msg);
    }
    return Promise.reject(error);
  },
);

export default request;
