// Layouts
export const layoutStyles = {
  pageRoot: "min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50",
  containerXl: "max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8",
  containerLg: "max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8",
  containerMd: "max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8",
};

// Headers
export const headerStyles = {
  wrapper: "mb-8",
  content: "flex items-center gap-3 mb-3",
  icon: "w-10 h-10 rounded-lg flex items-center justify-center text-white shadow-md",
  title: "text-4xl font-bold bg-clip-text text-transparent",
  description: "text-gray-600 font-medium ml-[3.25rem]",
};

// Cards
export const cardStyles = {
  base: "border-2 shadow-xl bg-white overflow-hidden rounded-xl",
  headerGradient: "border-b p-6",
};

// Buttons
export const buttonStyles = {
  back: "mb-6 hover:bg-blue-50 text-gray-700 font-medium",
  submit:
    "w-full h-12 shadow-lg hover:shadow-xl transition-all text-base font-bold text-white disabled:opacity-50 disabled:cursor-not-allowed",
};

// Inputs
export const inputStyles = {
  base: "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-base ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium file:text-foreground placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 md:text-sm",
  auth: "w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition-all",
};

// Themes
export const themeStyles = {
  primary: {
    bg: "bg-gradient-to-r from-blue-600 to-purple-600",
    text: "bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent",
    headerBg: "bg-gradient-to-r from-blue-50 to-purple-50",
    btn: "bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700",
  },
  success: {
    bg: "bg-gradient-to-r from-green-600 to-teal-600",
    text: "bg-gradient-to-r from-green-600 to-teal-600 bg-clip-text text-transparent",
    headerBg: "bg-gradient-to-r from-green-50 to-teal-50",
    btn: "bg-gradient-to-r from-green-600 to-teal-600 hover:from-green-700 hover:to-teal-700",
  },
  warning: {
    bg: "bg-gradient-to-r from-amber-500 to-orange-500",
    text: "bg-gradient-to-r from-amber-500 to-orange-500 bg-clip-text text-transparent",
    headerBg: "bg-gradient-to-r from-amber-50 to-orange-50",
  },
  info: {
    bg: "bg-gradient-to-r from-blue-600 to-indigo-600",
    text: "bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent",
    headerBg: "bg-gradient-to-r from-blue-50 to-indigo-50",
  },
};
