"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { challengeService } from "@/services/challengeService";
import { ChallengeRequest, ChallengeSearchRequest } from "@/types/challenge";

// --- Queries ---

export const useChallenges = (params?: ChallengeSearchRequest) => {
    return useQuery({
        queryKey: ["challenges", params],
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

export const useChallenge = (id: number) => {
    return useQuery({
        queryKey: ["challenge", id],
        queryFn: () => challengeService.getChallenge(id),
        enabled: !!id,
        select: (data) => data.data,
    });
};

export const useParticipants = (id: number) => {
    return useQuery({
        queryKey: ["challenge", id, "participants"],
        queryFn: () => challengeService.getParticipants(id),
        enabled: !!id,
        select: (data) => data.data,
    });
};

export const useChallengeStatistics = (id: number) => {
    return useQuery({
        queryKey: ["challenge", id, "statistics"],
        queryFn: () => challengeService.getStatistics(id),
        enabled: !!id,
        select: (data) => data.data,
    });
};

// --- Mutations ---

export const useCreateChallenge = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (data: ChallengeRequest) => challengeService.createChallenge(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["challenges"] });
        },
    });
};

export const useUpdateChallenge = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ id, data }: { id: number; data: ChallengeRequest }) =>
            challengeService.updateChallenge(id, data),
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({ queryKey: ["challenges"] });
            queryClient.invalidateQueries({ queryKey: ["challenge", variables.id] });
        },
    });
};

export const useDeleteChallenge = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: number) => challengeService.deleteChallenge(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["challenges"] });
        },
    });
};

export const useJoinChallenge = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: number) => challengeService.joinChallenge(id),
        onSuccess: (_, id) => {
            queryClient.invalidateQueries({ queryKey: ["challenge", id] });
            queryClient.invalidateQueries({ queryKey: ["challenge", id, "participants"] });
        },
    });
};

export const useWithdrawChallenge = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: number) => challengeService.withdrawChallenge(id),
        onSuccess: (_, id) => {
            queryClient.invalidateQueries({ queryKey: ["challenge", id] });
            queryClient.invalidateQueries({ queryKey: ["challenge", id, "participants"] });
        },
    });
};