import axios from 'axios';
import { message } from 'antd';
import { useAuthStore } from '@/store/auth';
import { useUserAuthStore } from '@/store/userAuthStore';

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
  // 优先使用C端用户token，其次管理端token
  const userToken = useUserAuthStore.getState().token;
  const adminToken = useAuthStore.getState().token;
  const token = userToken || adminToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      // 根据当前路径判断清除哪个auth
      const isAdmin = window.location.pathname.startsWith('/admin');
      if (isAdmin) {
        useAuthStore.getState().clearAuth();
        window.location.href = '/admin/login';
      } else {
        useUserAuthStore.getState().clearAuth();
        // C端不清除管理端auth，也不跳转
      }
    } else {
      const msg = error.response?.data?.message || '请求失败，请稍后重试';
      message.error(msg);
    }
    return Promise.reject(error);
  },
);

export default request;
