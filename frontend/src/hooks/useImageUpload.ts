import { useState, useCallback, useEffect } from "react";
import { handleImageProcess } from "@/lib/imageUtils";

interface UseImageUploadReturn {
  file: File | null;
  previewUrl: string | null;
  isCompressing: boolean;
  error: string | null;
  handleFileChange: (e: React.ChangeEvent<HTMLInputElement>) => Promise<void>;
  setFile: (file: File | null) => void;
  setPreviewUrl: (url: string | null) => void;
  reset: () => void;
}

export const useImageUpload = (initialPreviewUrl: string | null = null): UseImageUploadReturn => {
  const [file, setFileState] = useState<File | null>(null);
  const [previewUrl, setPreviewUrlState] = useState<string | null>(initialPreviewUrl);
  const [isCompressing, setIsCompressing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 초기 URL 변경 시 반영 (ex: 데이터 로딩 후)
  useEffect(() => {
    if (initialPreviewUrl) {
      setPreviewUrlState(initialPreviewUrl);
    }
  }, [initialPreviewUrl]);

  const setFile = useCallback((newFile: File | null) => {
    setFileState(newFile);
  }, []);

  const setPreviewUrl = useCallback((url: string | null) => {
    setPreviewUrlState(url);
  }, []);

  const handleFileChange = useCallback(
    async (e: React.ChangeEvent<HTMLInputElement>) => {
      const selectedFile = e.target.files?.[0];
      setError(null);

      if (!selectedFile) {
        // 파일 선택 취소 시 기존 상태 유지? 혹은 초기화?
        // 보통 취소하면 아무 일도 안 일어나거나 초기화. 여기서는 초기화하지 않음.
        return;
      }

      setIsCompressing(true);
      const { file: processedFile, error: processError } = await handleImageProcess(selectedFile);
      setIsCompressing(false);

      if (processError) {
        setError(processError);
        e.target.value = ""; // 입력 초기화
        return;
      }

      if (processedFile) {
        setFileState(processedFile);
        // 기존 미리보기 URL 해제 (메모리 누수 방지)
        if (previewUrl && !previewUrl.startsWith("http")) {
          URL.revokeObjectURL(previewUrl);
        }
        const newUrl = URL.createObjectURL(processedFile);
        setPreviewUrlState(newUrl);
      }

      // 같은 파일 다시 선택 가능하게 초기화
      e.target.value = "";
    },
    [previewUrl]
  );

  const reset = useCallback(() => {
    setFileState(null);
    setPreviewUrlState(null);
    setError(null);
    setIsCompressing(false);
  }, []);

  // 컴포넌트 언마운트 시 미리보기 URL 정리
  useEffect(() => {
    return () => {
      if (previewUrl && !previewUrl.startsWith("http")) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  return {
    file,
    previewUrl,
    isCompressing,
    error,
    handleFileChange,
    setFile,
    setPreviewUrl,
    reset,
  };
};
