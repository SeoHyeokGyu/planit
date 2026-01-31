"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { challengeService } from "@/services/challengeService";
import { ChallengeRequest, ChallengeSearchRequest } from "@/types/challenge";

// --- Queries ---

export const useChallenges = (params?: ChallengeSearchRequest) => {
  return useQuery({
    queryKey: ["challenges", params?.category, params?.difficulty, params?.sortBy, params?.page, params?.size],
    queryFn: () => challengeService.getChallenges(params),
    select: (data) => data.data, // ApiResponse<T>에서 data 추출
  });
};

export const useSearchChallenges = (keyword: string) => {
  return useQuery({
    queryKey: ["challenges", "search", keyword],
    queryFn: () => challengeService.searchChallenges(keyword),
    enabled: keyword.length > 0,
    select: (data) => data.data,
  });
};

export const useChallenge = (id: string) => {
  return useQuery({
    queryKey: ["challenge", id],
    queryFn: () => challengeService.getChallenge(id),
    enabled: !!id,
    select: (data) => data.data,
  });
};

export const useParticipants = (id: string) => {
  return useQuery({
    queryKey: ["challenge", id, "participants"],
    queryFn: () => challengeService.getParticipants(id),
    enabled: !!id,
    select: (data) => data.data,
  });
};

export const useChallengeStatistics = (id: string) => {
  return useQuery({
    queryKey: ["challenge", id, "statistics"],
    queryFn: () => challengeService.getStatistics(id),
    enabled: !!id,
    select: (data) => data.data,
  });
};

export const useMyChallenges = () => {
  return useQuery({
    queryKey: ["myChallenges"],
    queryFn: () => challengeService.getMyChallenges(),
    select: (data) => data.data,
  });
};

export const useNewChallengeRecommendations = (options?: { enabled?: boolean }) => {
  return useQuery({
    queryKey: ["challengeRecommendations", "new"],
    queryFn: () => challengeService.getNewChallengeRecommendations(),
    select: (data) => data.data,
    staleTime: 1000 * 60 * 5, // 5 minutes cache
    retry: 1,
    enabled: options?.enabled ?? true,
  });
};

export const useNewChallengeRecommendationsWithQuery = (query: string) => {
  return useQuery({
    queryKey: ["challengeRecommendations", "new", query],
    queryFn: () => challengeService.getNewChallengeRecommendationsWithQuery(query),
    select: (data) => data.data,
    enabled: !!query,
    staleTime: 1000 * 60 * 5,
    retry: 1,
  });
};

export const useRecommendedExistingChallenges = (options?: { enabled?: boolean }) => {
  return useQuery({
    queryKey: ["challengeRecommendations", "existing"],
    queryFn: () => challengeService.getRecommendedExistingChallenges(),
    select: (data) => data.data,
    staleTime: 1000 * 60 * 5, // 5 minutes cache
    retry: 1,
    enabled: options?.enabled ?? true,
  });
};

export const useRecommendedExistingChallengesWithQuery = (query: string) => {
  return useQuery({
    queryKey: ["challengeRecommendations", "existing", query],
    queryFn: () => challengeService.getRecommendedExistingChallengesWithQuery(query),
    select: (data) => data.data,
    enabled: !!query, // 쿼리가 있을 때만 실행
    staleTime: 1000 * 60 * 5,
    retry: 1,
  });
};

// --- Mutations ---

export const useCreateChallenge = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: ChallengeRequest) => challengeService.createChallenge(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["challenges"] });
      toast.success("챌린지가 생성되었습니다.");
    },
    onError: (error) => {
      toast.error(error.message || "챌린지 생성 실패");
    },
  });
};

export const useUpdateChallenge = (challengeId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: ChallengeRequest) => challengeService.updateChallenge(challengeId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["challenges"] });
      queryClient.invalidateQueries({ queryKey: ["challenge", challengeId] });
      queryClient.invalidateQueries({ queryKey: ["myChallenges"] });
      // 토스트는 컴포넌트에서 처리 (자동 저장이므로)
    },
    onError: (error) => {
      toast.error(error.message || "챌린지 수정 실패");
    },
  });
};

export const useDeleteChallenge = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => challengeService.deleteChallenge(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["challenges"] });
      toast.success("챌린지가 삭제되었습니다.");
    },
    onError: (error) => {
      toast.error(error.message || "챌린지 삭제 실패");
    },
  });
};

export const useJoinChallenge = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => challengeService.joinChallenge(id),
    onSuccess: (data, id) => {
      queryClient.invalidateQueries({ queryKey: ["challenge", id] });
      queryClient.invalidateQueries({ queryKey: ["challenge", id, "participants"] });
      toast.success("챌린지에 참여했습니다.");
    },
    onError: (error) => {
      toast.error(error.message || "참여 실패");
    },
  });
};

export const useWithdrawChallenge = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => challengeService.withdrawChallenge(id),
    onSuccess: (data, id) => {
      queryClient.invalidateQueries({ queryKey: ["challenge", id] });
      queryClient.invalidateQueries({ queryKey: ["challenge", id, "participants"] });
      toast.success("챌린지를 포기했습니다.");
    },
    onError: (error) => {
      toast.error(error.message || "포기 실패");
    },
  });
};