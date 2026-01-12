"use client";

import { certificationService } from "@/services/certificationService";
import { Page } from "@/types/api";
import { CertificationCreateRequest, CertificationUpdateRequest } from "@/types/certification";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

// --- Queries ---

export const useCertification = (id: number) => {
  return useQuery({
    queryKey: ["certification", id],
    queryFn: () => certificationService.getCertification(id),
    enabled: !!id,
    select: (data) => data.data,
  });
};

export const useCertificationsByUser = (
    userLoginId: string,
    page: number = 0,
    size: number = 10,
    options?: { enabled: boolean }
) => {
  return useQuery({
    queryKey: ["certifications", "user", userLoginId, page, size],
    queryFn: () => certificationService.getCertificationsByUser(userLoginId, page, size),
    enabled: !!userLoginId,
    select: (data): Page<any> => ({
      content: data.data,
      totalElements: data.pagination?.totalElements || 0,
      totalPages: data.pagination?.totalPages || 0,
      number: data.pagination?.pageNumber || 0,
      size: data.pagination?.pageSize || 10,
    }),
    ...options
  });
};

export const useCertificationsByChallenge = (challengeId: string, page: number = 0, size: number = 10) => {
  return useQuery({
    queryKey: ["certifications", "challenge", challengeId, page, size],
    queryFn: () => certificationService.getCertificationsByChallenge(challengeId, page, size),
    enabled: !!challengeId,
    select: (data): Page<any> => ({
      content: data.data,
      totalElements: data.pagination?.totalElements || 0,
      totalPages: data.pagination?.totalPages || 0,
      number: data.pagination?.pageNumber || 0,
      size: data.pagination?.pageSize || 10,
    }),
  });
};

export const useCertificationsByDateRange = (
  userLoginId: string,
  from: string,
  to: string,
  options?: { enabled: boolean }
) => {
  return useQuery({
    queryKey: ["certifications", "date-range", userLoginId, from, to],
    queryFn: () => certificationService.getCertificationsByDateRange(userLoginId, from, to),
    enabled: !!userLoginId && !!from && !!to,
    select: (data) => data.data,
    ...options
  });
};

// --- Mutations ---

export const useCreateCertification = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CertificationCreateRequest) => certificationService.createCertification(data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["certifications"] });
      queryClient.invalidateQueries({ queryKey: ["challenge", variables.challengeId] });
      toast.success("인증글이 작성되었습니다.");
    },
  });
};

export const useUploadCertificationPhoto = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, file }: { id: number; file: File }) =>
      certificationService.uploadPhoto(id, file),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["certification", variables.id] });
      queryClient.invalidateQueries({ queryKey: ["certifications"] });
      toast.success("사진이 업로드되었습니다.");
    },
  });
};

export const useUpdateCertification = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: CertificationUpdateRequest }) =>
      certificationService.updateCertification(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["certification", variables.id] });
      queryClient.invalidateQueries({ queryKey: ["certifications"] });
      toast.success("인증글이 수정되었습니다.");
    },
  });
};

export const useDeleteCertification = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, challengeId }: { id: number; challengeId?: string }) =>
      certificationService.deleteCertification(id),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["certifications"] });
      if (variables.challengeId) {
        queryClient.invalidateQueries({ queryKey: ["challenge", variables.challengeId] });
      }
      toast.success("인증글이 삭제되었습니다.");
    },
  });
};
