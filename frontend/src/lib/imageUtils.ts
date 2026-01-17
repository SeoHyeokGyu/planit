import imageCompression from "browser-image-compression";
import { toast } from "sonner";

// 허용된 이미지 확장자 목록
export const ALLOWED_IMAGE_EXTENSIONS = ["jpg", "jpeg", "png", "gif", "webp", "bmp"];
export const ALLOWED_IMAGE_EXTENSIONS_STRING = ".jpg,.jpeg,.png,.gif,.webp,.bmp";

// 최대 파일 크기 (10MB)
export const MAX_FILE_SIZE_MB = 10;
export const MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;

interface ValidationResult {
  isValid: boolean;
  message?: string;
}

/**
 * 이미지 파일의 유효성을 검사합니다. (확장자, 크기)
 */
export const validateImage = (file: File): ValidationResult => {
  // 1. 확장자 검사
  const fileName = file.name.toLowerCase();
  const isValidExtension = ALLOWED_IMAGE_EXTENSIONS.some((ext) => fileName.endsWith(`.${ext}`));

  if (!isValidExtension) {
    return {
      isValid: false,
      message: `허용되지 않는 파일 형식입니다. (${ALLOWED_IMAGE_EXTENSIONS.join(", ")})`,
    };
  }

  // 2. 크기 검사 (압축 전 원본 크기 체크, 너무 크면 브라우저 부하 방지 위해 차단할 수도 있음)
  // 여기서는 10MB 넘어가면 경고하되, 압축 시도할 수 있도록 허용할지 결정해야 함.
  // 보통 압축 라이브러리는 큰 파일도 처리하지만, 너무 크면 OOM 발생 가능.
  // 일단 20MB 이상은 아예 차단하고, 10~20MB는 압축 시도하는 식으로 정책을 정할 수 있음.
  // 여기서는 단순히 MAX_FILE_SIZE_BYTES 기준으로 검사하되, 압축하면 줄어들 것이므로
  // 원본이 너무 크면(예: 30MB) 차단하는 용도로 사용.
  const ABSOLUTE_MAX_SIZE = 30 * 1024 * 1024; // 30MB
  if (file.size > ABSOLUTE_MAX_SIZE) {
    return {
      isValid: false,
      message: "파일 크기가 너무 큽니다. 30MB 이하의 이미지만 업로드 가능합니다.",
    };
  }

  return { isValid: true };
};

/**
 * 이미지를 압축합니다.
 * @param file 원본 파일
 * @returns 압축된 파일 (실패 시 원본 반환 또는 에러 throw)
 */
export const compressImage = async (file: File): Promise<File> => {
  // GIF는 압축 시 애니메이션이 깨질 수 있으므로 원본 반환
  if (file.type === "image/gif" || file.name.toLowerCase().endsWith(".gif")) {
    return file;
  }

  const options = {
    maxSizeMB: 1, // 최대 1MB 목표
    maxWidthOrHeight: 1920, // 최대 해상도 1920px
    useWebWorker: true, // 웹 워커 사용 (UI 블로킹 방지)
    fileType: file.type as string, // 원본 타입 유지 (가능하면)
  };

  try {
    const compressedFile = await imageCompression(file, options);
    // console.log(`이미지 압축 성공: ${file.size / 1024 / 1024}MB -> ${compressedFile.size / 1024 / 1024}MB`);

    // 압축된 Blob(File)의 이름이 'blob' 등으로 변경될 수 있으므로 원본 이름으로 다시 래핑
    return new File([compressedFile], file.name, {
      type: compressedFile.type,
      lastModified: Date.now(),
    });
  } catch (error) {
    console.error("이미지 압축 실패:", error);
    // 압축 실패 시 원본 파일 반환 (혹은 에러 처리)
    // 여기서는 원본을 그대로 업로드하도록 처리
    return file;
  }
};

/**
 * 파일 핸들러 헬퍼 함수
 * 검증 및 압축을 수행하고 결과를 반환합니다.
 */
export const handleImageProcess = async (
  file: File
): Promise<{ file: File | null; error?: string }> => {
  // 1. 검증
  const validation = validateImage(file);
  if (!validation.isValid) {
    toast.error(validation.message);
    return { file: null, error: validation.message };
  }

  // 2. 압축
  try {
    const compressedFile = await compressImage(file);
    return { file: compressedFile };
  } catch (e) {
    // 압축 실패해도 검증 통과했으면 원본 사용
    return { file: file };
  }
};
