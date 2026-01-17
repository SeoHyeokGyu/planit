"use client";

import { feedService } from "@/services/feedService";
import { useAuthStore } from "@/stores/authStore";
import { Page } from "@/types/api";
import { FeedResponse, FeedSortType } from "@/types/feed";
import { useQuery, useInfiniteQuery } from "@tanstack/react-query";

export const useFeed = (page: number = 0, size: number = 10, sortBy: FeedSortType = "LATEST") => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  return useQuery({
    queryKey: ["feed", page, size, sortBy],
    queryFn: () => feedService.getFeed(page, size, sortBy),
    enabled: isAuthenticated,
    select: (response): Page<FeedResponse> => ({
      content: response.data || [],
      totalElements: response.pagination?.totalElements || 0,
      totalPages: response.pagination?.totalPages || 0,
      number: response.pagination?.pageNumber || 0,
      size: response.pagination?.pageSize || 10,
    }),
  });
};

export const useFeedInfinite = (size: number = 10, sortBy: FeedSortType = "LATEST") => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  return useInfiniteQuery({
    queryKey: ["feed", "infinite", sortBy],
    queryFn: ({ pageParam = 0 }) => feedService.getFeed(pageParam, size, sortBy),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      const pagination = lastPage.pagination;
      if (!pagination) return undefined;
      return pagination.pageNumber < pagination.totalPages - 1
        ? pagination.pageNumber + 1
        : undefined;
    },
    enabled: isAuthenticated,
  });
};
