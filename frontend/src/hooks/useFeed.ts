"use client";

import { feedService } from "@/services/feedService";
import { Page } from "@/types/api";
import { CertificationResponse } from "@/types/certification";
import { useQuery } from "@tanstack/react-query";

export const useFeed = (page: number = 0, size: number = 10) => {
  return useQuery({
    queryKey: ["feed", page, size],
    queryFn: () => feedService.getFeed(page, size),
    select: (data): Page<CertificationResponse> => ({
      content: data.data?.content || [],
      totalElements: data.data?.totalElements || 0,
      totalPages: data.data?.totalPages || 0,
      number: data.data?.number || 0,
      size: data.data?.size || 10,
    }),
  });
};
