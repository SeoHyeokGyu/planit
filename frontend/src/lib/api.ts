import { useAuthStore } from "@/stores/authStore"; // Import useAuthStore

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export const api = {
  baseURL: API_BASE_URL,

  async fetch<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const token = typeof window !== "undefined"
      ? useAuthStore.getState().token // Get token from Zustand store
      : null;

    const isFormData = options.body instanceof FormData;

    const headers: HeadersInit = {
      ...(!isFormData && { "Content-Type": "application/json" }),
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options.headers,
    };

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
    });

    const result = await response.json();

    if (!response.ok) {
      if (response.status === 401) {
        useAuthStore.getState().clearToken();
      }

      const errorMessage = result?.error?.message || result?.message || `HTTP error! status: ${response.status}`;
      throw new Error(errorMessage);
    }

    return result;
  },

  get<T>(endpoint: string, options?: RequestInit): Promise<T> {
    return this.fetch<T>(endpoint, { ...options, method: "GET" });
  },

  post<T>(endpoint: string, data?: unknown, options?: RequestInit): Promise<T> {
    const isFormData = data instanceof FormData;
    return this.fetch<T>(endpoint, {
      ...options,
      method: "POST",
      body: isFormData ? (data as FormData) : JSON.stringify(data),
    });
  },

  put<T>(endpoint: string, data?: unknown, options?: RequestInit): Promise<T> {
    const isFormData = data instanceof FormData;
    return this.fetch<T>(endpoint, {
      ...options,
      method: "PUT",
      body: isFormData ? (data as FormData) : JSON.stringify(data),
    });
  },

  patch<T>(endpoint: string, data?: unknown, options?: RequestInit): Promise<T> {
    const isFormData = data instanceof FormData;
    return this.fetch<T>(endpoint, {
      ...options,
      method: "PATCH",
      body: isFormData ? (data as FormData) : JSON.stringify(data),
    });
  },

  delete<T>(endpoint: string, options?: RequestInit): Promise<T> {
    return this.fetch<T>(endpoint, { ...options, method: "DELETE" });
  },
};
