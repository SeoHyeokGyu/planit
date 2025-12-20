import { create } from "zustand";
import { persist } from "zustand/middleware";

interface AuthState {
  token: string | null;
  userId: number | null;
  loginId: string | null;
  isAuthenticated: boolean;
  setToken: (token: string, userId?: number, loginId?: string) => void;
  clearToken: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      userId: null,
      loginId: null,
      isAuthenticated: false,
      setToken: (token: string, userId?: number, loginId?: string) =>
        set({ token, userId: userId || null, loginId: loginId || null, isAuthenticated: true }),
      clearToken: () =>
        set({ token: null, userId: null, loginId: null, isAuthenticated: false }),
    }),
    {
      name: "auth-storage",
    }
  )
);
