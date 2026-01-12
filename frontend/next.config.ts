import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactStrictMode: true,
  output: 'standalone',
  async rewrites() {
    const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
    return [
      {
        source: "/images/:path*",
        destination: `${API_BASE_URL}/images/:path*`,
      },
    ];
  },
};

export default nextConfig;
