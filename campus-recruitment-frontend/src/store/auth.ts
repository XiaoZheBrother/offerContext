import { create } from 'zustand';
import Cookies from 'js-cookie';

interface AuthState {
  token: string | null;
  username: string | null;
  setAuth: (token: string, username: string) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: Cookies.get('token') || null,
  username: Cookies.get('username') || null,
  setAuth: (token, username) => {
    Cookies.set('token', token, { expires: 7 });
    Cookies.set('username', username, { expires: 7 });
    set({ token, username });
  },
  clearAuth: () => {
    Cookies.remove('token');
    Cookies.remove('username');
    set({ token: null, username: null });
  },
}));
