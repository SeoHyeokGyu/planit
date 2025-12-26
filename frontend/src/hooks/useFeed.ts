"use client";

import { feedService } from "@/services/feedService";
import { Page } from "@/types/api";
import { FeedResponse } from "@/types/feed";
import { useQuery } from "@tanstack/react-query";

export const useFeed = (page: number = 0, size: number = 10) => {
  return useQuery({
    queryKey: ["feed", page, size],
    queryFn: () => feedService.getFeed(page, size),
    select: (response): Page<FeedResponse> => ({
      content: response.data || [],
      totalElements: response.pagination?.totalElements || 0,
      totalPages: response.pagination?.totalPages || 0,
      number: response.pagination?.pageNumber || 0,
      size: response.pagination?.pageSize || 10,
    }),
  });
};
