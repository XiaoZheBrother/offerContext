import { create } from 'zustand';

interface User {
  id: number;
  email: string;
  nickname: string;
  avatarUrl: string;
}

interface UserAuthState {
  token: string | null;
  user: User | null;
  setAuth: (token: string, user: User) => void;
  clearAuth: () => void;
  updateUser: (user: Partial<User>) => void;
}

const TOKEN_KEY = 'user_token';
const USER_KEY = 'user_info';

function getStoredAuth() {
  const token = localStorage.getItem(TOKEN_KEY);
  const userStr = localStorage.getItem(USER_KEY);
  const user = userStr ? JSON.parse(userStr) : null;
  return { token, user };
}

export const useUserAuthStore = create<UserAuthState>((set, get) => ({
  ...getStoredAuth(),
  setAuth: (token, user) => {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    set({ token, user });
  },
  clearAuth: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    set({ token: null, user: null });
  },
  updateUser: (updates) => {
    const current = get().user;
    if (current) {
      const updated = { ...current, ...updates };
      localStorage.setItem(USER_KEY, JSON.stringify(updated));
      set({ user: updated });
    }
  },
}));
