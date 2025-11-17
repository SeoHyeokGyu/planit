import Link from 'next/link';

export default function Home() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-b from-blue-50 to-white">
      <div className="text-center">
        <h1 className="text-6xl font-bold text-blue-600 mb-4">Planit</h1>
        <p className="text-xl text-gray-600 mb-2">여행 플래너 애플리케이션</p>
        <p className="text-gray-500 mb-8">Next.js + TypeScript + Tailwind CSS</p>

        <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
          <Link
            href="/api-test"
            className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-semibold"
          >
            API 테스트 →
          </Link>
          <a
            href="https://planit-api-y2ie.onrender.com/swagger-ui/index.html"
            target="_blank"
            rel="noopener noreferrer"
            className="px-6 py-3 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition-colors font-semibold"
          >
            Swagger UI →
          </a>
        </div>

        <div className="mt-12 p-4 bg-white rounded-lg shadow-sm max-w-md mx-auto">
          <p className="text-sm text-gray-600 mb-2">백엔드 서버:</p>
          <code className="text-xs bg-gray-100 px-3 py-1 rounded">
            https://planit-api-y2ie.onrender.com
          </code>
        </div>
      </div>
    </div>
  );
}
