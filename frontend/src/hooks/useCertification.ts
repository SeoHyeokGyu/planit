"use client";

import { useMutation, useQuery, useQueryClient, UseQueryOptions, QueryKey } from "@tanstack/react-query";
import { certificationService } from "@/services/certificationService";
import { CertificationCreateRequest, CertificationUpdateRequest, CertificationResponse } from "@/types/certification";
import { ApiResponse, Page } from "@/types/api";

// Define internal options types for better type inference and control
type CertificationsByUserInternalOptions = UseQueryOptions<
  ApiResponse<Page<CertificationResponse>>,
  Error,
  Page<CertificationResponse>,
  QueryKey
>;

type CertificationsByDateRangeInternalOptions = UseQueryOptions<
  ApiResponse<CertificationResponse[]>,
  Error,
  CertificationResponse[],
  QueryKey
>;

// --- Queries ---

export const useCertification = (id: number) => {
  return useQuery({
    queryKey: ["certification", id],
    queryFn: () => certificationService.getCertification(id),
    enabled: !!id && !isNaN(id),
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
    select: (data) => data.data,
    ...options
  });
};

export const useCertificationsByChallenge = (challengeId: number, page: number = 0, size: number = 10) => {
  return useQuery({
    queryKey: ["certifications", "challenge", challengeId, page, size],
    queryFn: () => certificationService.getCertificationsByChallenge(challengeId, page, size),
    enabled: !!challengeId && !isNaN(challengeId),
    select: (data) => data.data,
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
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["certifications"] });
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
    },
  });
};

export const useDeleteCertification = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => certificationService.deleteCertification(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["certifications"] });
    },
  });
};
