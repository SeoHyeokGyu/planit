import Link from "next/link";
import { ArrowLeft } from "lucide-react";

export default function TermsPage() {
  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* 헤더 */}
        <div className="mb-8">
          <Link
            href="/"
            className="inline-flex items-center gap-2 text-gray-600 hover:text-gray-900 transition-colors mb-4"
          >
            <ArrowLeft className="w-4 h-4" />
            홈으로 돌아가기
          </Link>
          <h1 className="text-4xl font-bold text-gray-900 mb-4">
            이용약관
          </h1>
          <p className="text-gray-600">
            최종 수정일: 2026년 1월 31일
          </p>
        </div>

        {/* 약관 내용 */}
        <div className="bg-white rounded-lg shadow-sm p-8 space-y-8">
          <section>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              제1조 (목적)
            </h2>
            <p className="text-gray-700 leading-relaxed">
              본 약관은 Planit(이하 "회사")이 제공하는 소셜 챌린지 트래커 서비스(이하 "서비스")의
              이용과 관련하여 회사와 회원 간의 권리, 의무 및 책임사항, 기타 필요한 사항을 규정함을 목적으로 합니다.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              제2조 (정의)
            </h2>
            <div className="space-y-3 text-gray-700">
              <p>본 약관에서 사용하는 용어의 정의는 다음과 같습니다:</p>
              <ol className="list-decimal list-inside space-y-2 ml-4">
                <li>"서비스"란 회사가 제공하는 소셜 챌린지 트래커 플랫폼을 의미합니다.</li>
                <li>"회원"이란 서비스에 접속하여 본 약관에 따라 회사와 이용계약을 체결하고 서비스를 이용하는 고객을 의미합니다.</li>
                <li>"챌린지"란 회원이 목표 달성을 위해 설정한 활동 계획을 의미합니다.</li>
                <li>"인증"이란 회원이 챌린지 수행 내용을 기록하고 공유하는 행위를 의미합니다.</li>
              </ol>
            </div>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              제3조 (약관의 효력 및 변경)
            </h2>
            <div className="space-y-3 text-gray-700">
              <p>
                1. 본 약관은 서비스를 이용하고자 하는 모든 회원에 대하여 그 효력을 발생합니다.
              </p>
              <p>
                2. 회사는 필요한 경우 관련 법령을 위배하지 않는 범위에서 본 약관을 변경할 수 있으며,
                약관이 변경되는 경우 변경된 약관의 적용일자 및 변경사유를 명시하여 서비스 내 공지사항을 통해 고지합니다.
              </p>
            </div>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              제4조 (회원가입)
            </h2>
            <div className="space-y-3 text-gray-700">
              <p>
                1. 회원가입은 서비스 이용 희망자가 약관의 내용에 대하여 동의를 한 다음 회원가입 신청을 하고
                회사가 이러한 신청에 대하여 승낙함으로써 체결됩니다.
              </p>
              <p>
                2. 회원은 가입 시 정확하고 최신의 정보를 제공해야 하며,
                정보 변경 시 즉시 갱신해야 합니다.
              </p>
            </div>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              제5조 (서비스의 제공 및 변경)
            </h2>
            <div className="space-y-3 text-gray-700">
              <p>회사는 다음과 같은 서비스를 제공합니다:</p>
              <ol className="list-decimal list-inside space-y-2 ml-4">
                <li>챌린지 생성, 참여, 관리 서비스</li>
                <li>인증 기록 및 공유 서비스</li>
                <li>실시간 피드 및 소셜 기능</li>
                <li>랭킹 및 배지 시스템</li>
                <li>AI 기반 인증 분석 서비스</li>
                <li>기타 회사가 추가 개발하거나 다른 회사와의 제휴를 통해 제공하는 서비스</li>
              </ol>
            </div>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              제6조 (회원의 의무)
            </h2>
            <div className="space-y-3 text-gray-700">
              <p>회원은 다음 행위를 하여서는 안 됩니다:</p>
              <ol className="list-decimal list-inside space-y-2 ml-4">
                <li>신청 또는 변경 시 허위 내용의 등록</li>
                <li>타인의 정보 도용</li>
                <li>회사가 게시한 정보의 변경</li>
                <li>회사가 정한 정보 이외의 정보(컴퓨터 프로그램 등) 등의 송신 또는 게시</li>
                <li>회사와 기타 제3자의 저작권 등 지적재산권에 대한 침해</li>
                <li>회사 및 기타 제3자의 명예를 손상시키거나 업무를 방해하는 행위</li>
                <li>외설 또는 폭력적인 메시지, 화상, 음성, 기타 공서양속에 반하는 정보를 서비스에 공개 또는 게시하는 행위</li>
              </ol>
            </div>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              제7조 (서비스 이용의 제한 및 중지)
            </h2>
            <div className="space-y-3 text-gray-700">
              <p>
                회사는 회원이 본 약관의 의무를 위반하거나 서비스의 정상적인 운영을 방해한 경우,
                경고, 일시정지, 영구이용정지 등으로 서비스 이용을 단계적으로 제한할 수 있습니다.
              </p>
            </div>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              제8조 (면책조항)
            </h2>
            <div className="space-y-3 text-gray-700">
              <p>
                1. 회사는 천재지변 또는 이에 준하는 불가항력으로 인하여 서비스를 제공할 수 없는 경우에는
                서비스 제공에 관한 책임이 면제됩니다.
              </p>
              <p>
                2. 회사는 회원의 귀책사유로 인한 서비스 이용의 장애에 대하여는 책임을 지지 않습니다.
              </p>
              <p>
                3. 회사는 회원이 서비스를 이용하여 기대하는 수익을 상실한 것에 대하여 책임을 지지 않으며,
                그 밖에 서비스를 통하여 얻은 자료로 인한 손해에 관하여 책임을 지지 않습니다.
              </p>
            </div>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              제9조 (분쟁의 해결)
            </h2>
            <div className="space-y-3 text-gray-700">
              <p>
                1. 회사는 회원으로부터 제출되는 불만사항 및 의견을 우선적으로 처리합니다.
              </p>
              <p>
                2. 서비스 이용으로 발생한 분쟁에 대해 소송이 제기될 경우
                회사의 본사 소재지를 관할하는 법원을 관할 법원으로 합니다.
              </p>
            </div>
          </section>

          {/* 부칙 */}
          <section className="border-t pt-8">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              부칙
            </h2>
            <p className="text-gray-700">
              본 약관은 2026년 1월 31일부터 적용됩니다.
            </p>
          </section>
        </div>

        {/* 문의 */}
        <div className="mt-8 bg-blue-50 rounded-lg p-6">
          <h3 className="font-semibold text-gray-900 mb-2">
            약관 관련 문의
          </h3>
          <p className="text-gray-600 text-sm mb-4">
            이용약관에 대해 궁금하신 사항이 있으시면 언제든지 문의해주세요.
          </p>
          <a
            href="mailto:contact@planit.com"
            className="text-blue-600 hover:text-blue-700 text-sm font-medium"
          >
            contact@planit.com
          </a>
        </div>
      </div>
    </div>
  );
}
