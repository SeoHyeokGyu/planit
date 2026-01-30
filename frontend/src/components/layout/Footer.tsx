import Link from "next/link";
import { Github, Mail, FileText, Shield, Info } from "lucide-react";

export default function Footer() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="bg-gray-50 border-t border-gray-200 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* 서비스 소개 */}
          <div className="col-span-1 md:col-span-2">
            <Link href="/" className="inline-flex items-center mb-4">
              <span className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                Planit
              </span>
            </Link>
            <p className="text-gray-600 text-sm mb-4">
              AI 기반 소셜 챌린지 트래커
              <br />
              작은 성취를 실시간으로 공유하고, 함께 성장하세요.
            </p>
            <div className="flex gap-4">
              <a
                href="https://github.com/SeoHyeokGyu/planit"
                target="_blank"
                rel="noopener noreferrer"
                className="text-gray-500 hover:text-gray-700 transition-colors"
                aria-label="GitHub 저장소"
              >
                <Github className="w-5 h-5" />
              </a>
              <a
                href="mailto:contact@planit.com"
                className="text-gray-500 hover:text-gray-700 transition-colors"
                aria-label="이메일 문의"
              >
                <Mail className="w-5 h-5" />
              </a>
            </div>
          </div>

          {/* 서비스 */}
          <div>
            <h3 className="font-semibold text-gray-900 mb-4">서비스</h3>
            <ul className="space-y-2">
              <li>
                <Link
                  href="/challenge"
                  className="text-gray-600 hover:text-blue-600 transition-colors text-sm flex items-center gap-2"
                >
                  챌린지 둘러보기
                </Link>
              </li>
              <li>
                <Link
                  href="/feed"
                  className="text-gray-600 hover:text-blue-600 transition-colors text-sm flex items-center gap-2"
                >
                  실시간 피드
                </Link>
              </li>
              <li>
                <Link
                  href="/ranking"
                  className="text-gray-600 hover:text-blue-600 transition-colors text-sm flex items-center gap-2"
                >
                  랭킹
                </Link>
              </li>
            </ul>
          </div>

          {/* 정보 */}
          <div>
            <h3 className="font-semibold text-gray-900 mb-4">정보</h3>
            <ul className="space-y-2">
              <li>
                <Link
                  href="/about"
                  className="text-gray-600 hover:text-blue-600 transition-colors text-sm flex items-center gap-2"
                >
                  <Info className="w-4 h-4" />
                  서비스 소개
                </Link>
              </li>
              <li>
                <Link
                  href="/terms"
                  className="text-gray-600 hover:text-blue-600 transition-colors text-sm flex items-center gap-2"
                >
                  <FileText className="w-4 h-4" />
                  이용약관
                </Link>
              </li>
              <li>
                <Link
                  href="/privacy"
                  className="text-gray-600 hover:text-blue-600 transition-colors text-sm flex items-center gap-2"
                >
                  <Shield className="w-4 h-4" />
                  개인정보처리방침
                </Link>
              </li>
              <li>
                <a
                  href="mailto:contact@planit.com"
                  className="text-gray-600 hover:text-blue-600 transition-colors text-sm flex items-center gap-2"
                >
                  <Mail className="w-4 h-4" />
                  문의하기
                </a>
              </li>
            </ul>
          </div>
        </div>

        {/* 구분선 */}
        <div className="border-t border-gray-200 mt-8 pt-8">
          <div className="flex flex-col md:flex-row justify-between items-center gap-4">
            <p className="text-gray-500 text-sm">
              © {currentYear} Planit. All rights reserved.
            </p>
            <p className="text-gray-500 text-sm">
              Made with ❤️ by Planit Team
            </p>
          </div>
        </div>
      </div>
    </footer>
  );
}
