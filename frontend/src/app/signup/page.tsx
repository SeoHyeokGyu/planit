import { SignUpForm } from "@/components/auth/SignUpForm";

export default function SignUpPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gradient-to-br from-gray-50 to-blue-100 dark:from-gray-900 dark:to-blue-950 p-4">
      <div className="text-center mb-8">
        <h1 className="text-5xl font-bold text-blue-600">Planit</h1>
        <p className="text-gray-500 mt-2">새로운 여정을 시작해 보세요</p>
      </div>
      <SignUpForm />
    </div>
  );
}
