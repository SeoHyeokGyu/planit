"use client";

import { useState, useRef, useEffect } from "react";
import { useNotificationStore } from "@/stores/notificationStore";
import { Bell, Check } from "lucide-react";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

export default function NotificationDropdown() {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  
  const notifications = useNotificationStore((state) => state.notifications);
  const unreadCount = useNotificationStore((state) => state.unreadCount);
  const markAsRead = useNotificationStore((state) => state.markAsRead);
  const markAllAsRead = useNotificationStore((state) => state.markAllAsRead);

  // 외부 클릭 시 드롭다운 닫기
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  const handleNotificationClick = (id: string | undefined) => {
    if (id) {
        markAsRead(id);
    }
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        className="relative p-2 rounded-lg text-gray-700 hover:text-blue-600 hover:bg-blue-50 transition-all cursor-pointer"
        onClick={() => setIsOpen(!isOpen)}
        title="알림"
      >
        <Bell className="h-5 w-5" />
        {unreadCount > 0 && (
          <Badge
            variant="destructive"
            className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 text-[10px]"
          >
            {unreadCount > 99 ? "99+" : unreadCount}
          </Badge>
        )}
      </button>

      {isOpen && (
        <Card className="absolute right-0 mt-2 w-80 sm:w-96 max-h-[500px] overflow-hidden flex flex-col shadow-2xl z-[100] bg-white border border-gray-200 animate-in fade-in zoom-in-95 duration-200">
          <div className="p-4 border-b flex items-center justify-between bg-gray-50/80 backdrop-blur-sm sticky top-0 z-10">
            <div className="flex items-center gap-2">
              <h3 className="font-semibold text-sm text-gray-900">알림</h3>
              {unreadCount > 0 && (
                  <span className="text-xs text-blue-600 font-bold bg-blue-50 px-2 py-1 rounded-full">
                      {unreadCount} new
                  </span>
              )}
            </div>
            {notifications.length > 0 && unreadCount > 0 && (
              <button
                onClick={markAllAsRead}
                className="px-2 py-1 text-xs text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-all font-medium cursor-pointer flex items-center gap-1"
                title="모두 읽음으로 표시"
              >
                <Check className="h-3 w-3" />
                모두 읽음
              </button>
            )}
          </div>
          
          <div className="overflow-y-auto flex-1 p-2 space-y-2 max-h-[400px]">
            {notifications.length === 0 ? (
              <div className="py-12 text-center flex flex-col items-center justify-center text-gray-500">
                <Bell className="h-8 w-8 text-gray-300 mb-2" />
                <p className="text-sm">새로운 알림이 없습니다.</p>
              </div>
            ) : (
              notifications.map((notification) => (
                <div
                  key={notification.id || Math.random().toString()}
                  onClick={() => handleNotificationClick(notification.id)}
                  className={cn(
                    "p-3 rounded-xl text-sm transition-all duration-200 relative group border !cursor-pointer",
                    notification.read 
                      ? "bg-white border-transparent hover:bg-gray-50 text-gray-500" 
                      : "bg-blue-50/60 border-blue-100 hover:bg-blue-100/50 text-gray-900 shadow-sm"
                  )}
                >
                  <div className="flex justify-between items-start gap-3">
                    <p className="flex-1 leading-snug break-words">{notification.message}</p>
                    {!notification.read && (
                        <span className="h-2 w-2 rounded-full bg-blue-500 mt-1.5 shrink-0 animate-pulse shadow-[0_0_8px_rgba(59,130,246,0.5)]" />
                    )}
                  </div>
                  <div className="mt-2 flex items-center justify-between">
                    <span className="text-[11px] text-gray-400 font-medium">
                        {notification.clientTimestamp 
                            ? new Date(notification.clientTimestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) 
                            : new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                    {notification.type && (
                         <span className={cn(
                             "text-[10px] px-2 py-0.5 rounded-md font-semibold tracking-tight uppercase",
                             notification.type === 'INFO' ? "bg-gray-100 text-gray-600" :
                             notification.type === 'SUCCESS' ? "bg-green-100 text-green-700" :
                             notification.type === 'ERROR' ? "bg-red-100 text-red-700" :
                             "bg-gray-100 text-gray-600"
                         )}>
                             {notification.type}
                         </span>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        </Card>
      )}
    </div>
  );
}
