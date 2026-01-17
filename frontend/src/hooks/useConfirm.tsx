import { useState, useCallback, ReactNode } from "react";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";

interface ConfirmOptions {
  title: string;
  description: ReactNode;
  confirmLabel?: string;
  cancelLabel?: string;
  variant?: "default" | "destructive";
  isAlert?: boolean;
}

export const useConfirm = () => {
  const [promise, setPromise] = useState<{
    resolve: (value: boolean) => void;
  } | null>(null);

  const [options, setOptions] = useState<ConfirmOptions>({
    title: "",
    description: "",
    confirmLabel: "확인",
    cancelLabel: "취소",
    variant: "default",
    isAlert: false,
  });

  const confirm = useCallback(
    (newOptions: ConfirmOptions): Promise<boolean> => {
      setOptions({
        confirmLabel: "확인",
        cancelLabel: "취소",
        variant: "default",
        isAlert: false,
        ...newOptions,
      });
      return new Promise((resolve) => {
        setPromise({ resolve });
      });
    },
    []
  );

  const alert = useCallback(
    (newOptions: Omit<ConfirmOptions, "cancelLabel" | "isAlert">): Promise<void> => {
      return confirm({ ...newOptions, isAlert: true }).then(() => {});
    },
    [confirm]
  );

  const handleClose = () => {
    setPromise(null);
  };

  const handleConfirm = () => {
    promise?.resolve(true);
    handleClose();
  };

  const handleCancel = () => {
    promise?.resolve(false);
    handleClose();
  };

  const ConfirmDialog = () => (
    <AlertDialog open={promise !== null} onOpenChange={handleClose}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{options.title}</AlertDialogTitle>
          <AlertDialogDescription>{options.description}</AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          {!options.isAlert && (
            <AlertDialogCancel onClick={handleCancel}>
              {options.cancelLabel}
            </AlertDialogCancel>
          )}
          <AlertDialogAction
            onClick={handleConfirm}
            className={
              options.variant === "destructive"
                ? "bg-red-600 hover:bg-red-700 text-white"
                : ""
            }
          >
            {options.confirmLabel}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );

  return { confirm, alert, ConfirmDialog };
};
