import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";

interface AuthState {
  token: string | null;
  userId: number | null;
  loginId: string | null;
  isAuthenticated: boolean;
  _hasHydrated: boolean; // 스토리지 로딩 완료 여부
  setToken: (token: string, userId?: number, loginId?: string) => void;
  clearToken: () => void;
  setHasHydrated: (state: boolean) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      userId: null,
      loginId: null,
      isAuthenticated: false,
      _hasHydrated: false,
      setToken: (token: string, userId?: number, loginId?: string) =>
        set({ token, userId: userId || null, loginId: loginId || null, isAuthenticated: true }),
      clearToken: () => set({ token: null, userId: null, loginId: null, isAuthenticated: false }),
      setHasHydrated: (state: boolean) => set({ _hasHydrated: state }),
    }),
    {
      name: "auth-storage",
      storage: createJSONStorage(() => localStorage),
      onRehydrateStorage: () => (state) => {
        state?.setHasHydrated(true);
      },
    }
  )
);
