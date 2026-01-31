import { useQuery } from "@tanstack/react-query";
import { useAuthStore } from "@/stores/authStore";

export interface RandomUser {
  loginId: string;
  nickname: string | null;
  totalPoint: number;
}

async function fetchRandomUsers(token: string, size: number = 3): Promise<RandomUser[]> {
  const response = await fetch(
    `${process.env.NEXT_PUBLIC_API_URL}/api/users/random?size=${size}`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );

  if (!response.ok) {
    throw new Error("Failed to fetch random users");
  }

  const data = await response.json();
  return data.data;
}

export function useRandomUsers(size: number = 3) {
  const token = useAuthStore((state) => state.token);

  return useQuery({
    queryKey: ["randomUsers", size],
    queryFn: () => fetchRandomUsers(token!, size),
    enabled: !!token,
    staleTime: 5 * 60 * 1000, // 5분
  });
}
