import Link from "next/link";
import { ArrowLeft } from "lucide-react";

export default function PrivacyPage() {
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
            개인정보처리방침
          </h1>
          <p className="text-gray-600">
            최종 수정일: 2026년 1월 31일
          </p>
        </div>

        {/* 방침 내용 */}
        <div className="bg-white rounded-lg shadow-sm p-8 space-y-8">
          <section>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              1. 개인정보의 처리 목적
            </h2>
            <p className="text-gray-700 leading-relaxed mb-4">
              Planit(이하 "회사")은 다음의 목적을 위하여 개인정보를 처리합니다.
              처리하고 있는 개인정보는 다음의 목적 이외의 용도로는 이용되지 않으며,
              이용 목적이 변경되는 경우에는 개인정보 보호법 제18조에 따라 별도의 동의를 받는 등 필요한 조치를 이행할 예정입니다.
            </p>
            <ol className="list-decimal list-inside space-y-2 ml-4 text-gray-700">
              <li>회원 가입 및 관리: 회원 가입의사 확인, 회원제 서비스 제공에 따른 본인 식별·인증</li>
              <li>서비스 제공: 챌린지 생성 및 참여, 인증 기록, 피드 제공, 랭킹 산정</li>
              <li>마케팅 및 광고 활용: 신규 서비스 개발 및 맞춤 서비스 제공</li>
            </ol>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              2. 처리하는 개인정보 항목
            </h2>
            <div className="space-y-4 text-gray-700">
              <div>
                <h3 className="font-semibold text-gray-900 mb-2">필수 항목</h3>
                <ul className="list-disc list-inside ml-4 space-y-1">
                  <li>아이디(로그인 ID)</li>
                  <li>비밀번호</li>
                  <li>닉네임</li>
                  <li>이메일 주소</li>
                </ul>
              </div>
              <div>
                <h3 className="font-semibold text-gray-900 mb-2">선택 항목</h3>
                <ul className="list-disc list-inside ml-4 space-y-1">
                  <li>프로필 사진</li>
                  <li>자기소개</li>
                </ul>
              </div>
              <div>
                <h3 className="font-semibold text-gray-900 mb-2">자동 수집 항목</h3>
                <ul className="list-disc list-inside ml-4 space-y-1">
                  <li>서비스 이용 기록</li>
                  <li>접속 로그</li>
                  <li>접속 IP 정보</li>
                  <li>쿠키</li>
                </ul>
              </div>
            </div>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              3. 개인정보의 처리 및 보유 기간
            </h2>
            <div className="space-y-3 text-gray-700">
              <p>
                회사는 법령에 따른 개인정보 보유·이용기간 또는 정보주체로부터 개인정보를 수집 시에
                동의받은 개인정보 보유·이용기간 내에서 개인정보를 처리·보유합니다.
              </p>
              <ul className="list-disc list-inside ml-4 space-y-2">
                <li>회원 탈퇴 시까지 (단, 관계 법령에 따라 보존할 필요가 있는 경우 해당 기간 동안 보관)</li>
                <li>부정 이용 기록: 1년</li>
                <li>서비스 이용 기록: 3개월</li>
              </ul>
            </div>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              4. 개인정보의 제3자 제공
            </h2>
            <p className="text-gray-700 leading-relaxed">
              회사는 정보주체의 개인정보를 제1조(개인정보의 처리 목적)에서 명시한 범위 내에서만 처리하며,
              정보주체의 동의, 법률의 특별한 규정 등 개인정보 보호법 제17조에 해당하는 경우에만
              개인정보를 제3자에게 제공합니다.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              5. 개인정보 처리의 위탁
            </h2>
            <p className="text-gray-700 leading-relaxed">
              회사는 원활한 서비스 제공을 위해 다음과 같이 개인정보 처리업무를 외부 전문업체에 위탁하여 운영하고 있습니다.
            </p>
            <div className="mt-4 overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      수탁업체
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      위탁업무 내용
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  <tr>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      AWS
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-700">
                      클라우드 서버 제공 및 데이터 보관
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              6. 정보주체의 권리·의무 및 행사 방법
            </h2>
            <div className="space-y-3 text-gray-700">
              <p>정보주체는 회사에 대해 언제든지 다음 각 호의 개인정보 보호 관련 권리를 행사할 수 있습니다:</p>
              <ol className="list-decimal list-inside space-y-2 ml-4">
                <li>개인정보 열람 요구</li>
                <li>오류 등이 있을 경우 정정 요구</li>
                <li>삭제 요구</li>
                <li>처리정지 요구</li>
              </ol>
              <p className="mt-4">
                권리 행사는 회사에 대해 서면, 전화, 전자우편 등을 통하여 하실 수 있으며,
                회사는 이에 대해 지체없이 조치하겠습니다.
              </p>
            </div>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              7. 개인정보의 파기
            </h2>
            <div className="space-y-3 text-gray-700">
              <p>
                회사는 개인정보 보유기간의 경과, 처리목적 달성 등 개인정보가 불필요하게 되었을 때에는
                지체없이 해당 개인정보를 파기합니다.
              </p>
              <div className="mt-4">
                <h3 className="font-semibold text-gray-900 mb-2">파기 절차</h3>
                <p>
                  이용자가 입력한 정보는 목적 달성 후 별도의 DB에 옮겨져 내부 방침 및 기타 관련 법령에 따라
                  일정기간 저장된 후 혹은 즉시 파기됩니다.
                </p>
              </div>
              <div className="mt-4">
                <h3 className="font-semibold text-gray-900 mb-2">파기 방법</h3>
                <ul className="list-disc list-inside ml-4 space-y-1">
                  <li>전자적 파일 형태: 복구 및 재생되지 않도록 안전하게 삭제</li>
                  <li>종이 문서: 분쇄기로 분쇄하거나 소각</li>
                </ul>
              </div>
            </div>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              8. 개인정보 보호책임자
            </h2>
            <div className="bg-gray-50 rounded-lg p-6 text-gray-700">
              <p className="mb-4">
                회사는 개인정보 처리에 관한 업무를 총괄해서 책임지고,
                개인정보 처리와 관련한 정보주체의 불만처리 및 피해구제 등을 위하여 아래와 같이
                개인정보 보호책임자를 지정하고 있습니다.
              </p>
              <div className="space-y-2">
                <p><strong>개인정보 보호책임자</strong></p>
                <p>이메일: contact@planit.com</p>
                <p>
                  정보주체는 회사의 서비스를 이용하시면서 발생한 모든 개인정보 보호 관련 문의,
                  불만처리, 피해구제 등에 관한 사항을 개인정보 보호책임자에게 문의하실 수 있습니다.
                </p>
              </div>
            </div>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              9. 개인정보 처리방침 변경
            </h2>
            <p className="text-gray-700 leading-relaxed">
              이 개인정보 처리방침은 2026년 1월 31일부터 적용되며,
              법령 및 방침에 따른 변경내용의 추가, 삭제 및 정정이 있는 경우에는
              변경사항의 시행 7일 전부터 공지사항을 통하여 고지할 것입니다.
            </p>
          </section>

          {/* 부칙 */}
          <section className="border-t pt-8">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              부칙
            </h2>
            <p className="text-gray-700">
              본 방침은 2026년 1월 31일부터 시행됩니다.
            </p>
          </section>
        </div>

        {/* 문의 */}
        <div className="mt-8 bg-blue-50 rounded-lg p-6">
          <h3 className="font-semibold text-gray-900 mb-2">
            개인정보 관련 문의
          </h3>
          <p className="text-gray-600 text-sm mb-4">
            개인정보 처리방침에 대해 궁금하신 사항이 있으시면 언제든지 문의해주세요.
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
