// frontend/src/services/certificationService.ts
import { api } from "@/lib/api";
import { ApiResponse, Page } from "@/types/api";
import {
  CertificationCreateRequest,
  CertificationResponse,
  CertificationUpdateRequest,
} from "@/types/certification";

export const certificationService = {
  createCertification: async (
    data: CertificationCreateRequest
  ): Promise<ApiResponse<CertificationResponse>> => {
    return api.post("/api/certifications", data);
  },

  uploadPhoto: async (
    id: number,
    file: File
  ): Promise<ApiResponse<CertificationResponse>> => {
    const formData = new FormData();
    formData.append("file", file);
    return api.post(`/api/certifications/${id}/photo`, formData);
  },

  getCertification: async (id: number): Promise<ApiResponse<CertificationResponse>> => {
    return api.get(`/api/certifications/${id}`);
  },

  getCertificationsByUser: async (
    userLoginId: string,
    page: number = 0,
    size: number = 10
  ): Promise<ApiResponse<CertificationResponse[]>> => {
    return api.get(`/api/certifications/user/${userLoginId}?page=${page}&size=${size}`);
  },

  getCertificationsByChallenge: async (
    challengeId: number,
    page: number = 0,
    size: number = 10
  ): Promise<ApiResponse<CertificationResponse[]>> => {
    return api.get(`/api/certifications/challenge/${challengeId}?page=${page}&size=${size}`);
  },

  getCertificationsByDateRange: async (
    userLoginId: string,
    from: string,
    to: string
  ): Promise<ApiResponse<CertificationResponse[]>> => {
    return api.get(
      `/api/certifications/user/${userLoginId}/date-range?from=${from}&to=${to}`
    );
  },

  updateCertification: async (
    id: number,
    data: CertificationUpdateRequest
  ): Promise<ApiResponse<CertificationResponse>> => {
    return api.put(`/api/certifications/${id}`, data);
  },

  deleteCertification: async (id: number): Promise<ApiResponse<void>> => {
    return api.delete(`/api/certifications/${id}`);
  },
};
