import { create } from "zustand";

export interface Notification {
  id?: string; // 서버에서 제공하는 고유 ID
  type: "INFO" | "ERROR" | "SUCCESS" | string; // 알림 종류
  message: string;
  read: boolean;
  createdAt: string; // 서버에서 제공하는 생성 시간
  clientTimestamp?: Date; // 클라이언트에서 수신한 시간
}

interface NotificationState {
  notifications: Notification[];
  addNotification: (notification: Omit<Notification, 'read' | 'clientTimestamp'>) => void;
  markAsRead: (id: string) => void;
  unreadCount: number;
}

export const useNotificationStore = create<NotificationState>((set, get) => ({
  notifications: [],
  unreadCount: 0,
  addNotification: (notification) => {
    const newNotification: Notification = {
      ...notification,
      read: false,
      clientTimestamp: new Date(),
    };
    set((state) => ({
      notifications: [newNotification, ...state.notifications],
      unreadCount: state.unreadCount + 1,
    }));
  },
  markAsRead: (id: string) => {
    set((state) => ({
      notifications: state.notifications.map((n) =>
        n.id === id ? { ...n, read: true } : n
      ),
      unreadCount: get().notifications.filter(n => !n.read).length,
    }));
  },
}));
