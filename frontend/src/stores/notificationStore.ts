import { create } from "zustand";
import { toast } from "sonner";

export interface Notification {
  id?: string; // 서버에서 제공하는 고유 ID
  type: "INFO" | "ERROR" | "SUCCESS" | "NEW_FEED" | string; // 알림 종류
  message: string;
  read: boolean;
  createdAt: string; // 서버에서 제공하는 생성 시간
  clientTimestamp?: Date; // 클라이언트에서 수신한 시간
  feedId?: number; // NEW_FEED 타입일 때 피드 ID
}

interface NotificationState {
  notifications: Notification[];
  newFeedCount: number;
  addNotification: (notification: Omit<Notification, "read" | "clientTimestamp">) => void;
  markAsRead: (id: string) => void;
  markAllAsRead: () => void;
  clearNewFeedCount: () => void;
  unreadCount: number;
}

export const useNotificationStore = create<NotificationState>((set, get) => ({
  notifications: [],
  unreadCount: 0,
  newFeedCount: 0,
  addNotification: (notification) => {
    console.info("noti 수신함.", notification);

    // 토스트 알림 표시
    switch (notification.type) {
      case "SUCCESS":
        toast.success(notification.message);
        break;
      case "ERROR":
        toast.error(notification.message);
        break;
      case "NEW_FEED":
        toast.info(notification.message, {
          duration: 5000,
          action: notification.feedId
            ? {
                label: "보기",
                onClick: () => {
                  window.location.href = `/feed`;
                },
              }
            : undefined,
        });
        break;
      case "INFO":
      default:
        toast.info(notification.message);
        break;
    }

    const newNotification: Notification = {
      id: notification.id || crypto.randomUUID(),
      ...notification,
      read: false,
      clientTimestamp: new Date(),
    };
    set((state) => ({
      notifications: [newNotification, ...state.notifications],
      unreadCount: state.unreadCount + 1,
      newFeedCount:
        notification.type === "NEW_FEED"
          ? state.newFeedCount + 1
          : state.newFeedCount,
    }));
  },
  markAsRead: (id: string) => {
    const notification = get().notifications.find((n) => n.id === id);
    set((state) => {
      const updatedNotifications = state.notifications.map((n) =>
        n.id === id ? { ...n, read: true } : n
      );
      return {
        notifications: updatedNotifications,
        unreadCount: updatedNotifications.filter((n) => !n.read).length,
        newFeedCount:
          notification?.type === "NEW_FEED" && !notification.read
            ? Math.max(0, state.newFeedCount - 1)
            : state.newFeedCount,
      };
    });
  },
  markAllAsRead: () => {
    set((state) => ({
      notifications: state.notifications.map((n) => ({ ...n, read: true })),
      unreadCount: 0,
      newFeedCount: 0,
    }));
  },
  clearNewFeedCount: () => {
    set({ newFeedCount: 0 });
  },
}));
