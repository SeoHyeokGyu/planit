import { create } from "zustand";
import { persist } from "zustand/middleware";

interface AuthState {
  token: string | null;
  userId: number | null;
  isAuthenticated: boolean;
  setToken: (token: string, userId?: number) => void;
  clearToken: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      userId: null,
      isAuthenticated: false,
      setToken: (token: string, userId?: number) =>
        set({ token, userId: userId || null, isAuthenticated: true }),
      clearToken: () =>
        set({ token: null, userId: null, isAuthenticated: false }),
    }),
    {
      name: "auth-storage",
    }
  )
);
