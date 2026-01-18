import { LucideIcon } from "lucide-react";
import { Button } from "./button";

export interface EmptyStateProps {
  icon: LucideIcon;
  title: string;
  description?: string;
  actionLabel?: string;
  onAction?: () => void;
  variant?: "default" | "bordered";
}

export function EmptyState({
  icon: Icon,
  title,
  description,
  actionLabel,
  onAction,
  variant = "default",
}: EmptyStateProps) {
  const containerClass =
    variant === "bordered"
      ? "text-center py-20 bg-white rounded-2xl shadow-sm border-2 border-dashed border-gray-200"
      : "text-center py-20";

  return (
    <div className={containerClass}>
      <div className="inline-flex items-center justify-center w-16 h-16 bg-gray-50 rounded-full mb-4">
        <Icon className="w-8 h-8 text-gray-400" />
      </div>
      <p className="text-gray-700 text-lg font-semibold">{title}</p>
      {description && <p className="text-gray-500 text-sm mt-2 mb-6">{description}</p>}
      {actionLabel && onAction && (
        <Button onClick={onAction} className="bg-blue-600 hover:bg-blue-700">
          {actionLabel}
        </Button>
      )}
    </div>
  );
}
