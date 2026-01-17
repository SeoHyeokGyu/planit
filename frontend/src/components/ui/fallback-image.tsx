"use client";

import Image, { ImageProps } from "next/image";
import { useState, useEffect } from "react";
import { ImageOff } from "lucide-react";

interface FallbackImageProps extends ImageProps {
  fallbackText?: string;
}

/**
 * 이미지 로딩 실패 시 대체 UI를 보여주는 래퍼 컴포넌트입니다.
 * Next.js Image 컴포넌트의 모든 props를 지원합니다.
 */
export function FallbackImage({ alt, fallbackText, ...props }: FallbackImageProps) {
  const [error, setError] = useState(false);

  // src가 변경되면 에러 상태 초기화 (새로운 이미지를 로드할 수 있도록)
  useEffect(() => {
    setError(false);
  }, [props.src]);

  if (error) {
    return (
      <div 
        className={`flex flex-col items-center justify-center bg-gray-100 text-gray-400 w-full h-full ${props.className}`}
        style={{ ...props.style }} // fill 속성 등을 위한 스타일 유지
      >
        <ImageOff className="w-10 h-10 mb-2 opacity-50" />
        <span className="text-xs font-medium text-gray-500">{fallbackText || "이미지를 불러올 수 없습니다"}</span>
      </div>
    );
  }

  return (
    <Image
      {...props}
      alt={alt}
      onError={() => setError(true)}
    />
  );
}
