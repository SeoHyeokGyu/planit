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

export const pageHeaderStyles = {
  container: "mb-8",
  wrapper: "flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4",
  titleSection: "flex-1",
  titleWrapper: "flex items-center gap-3 mb-3",
  iconBase: "w-10 h-10 rounded-lg flex items-center justify-center text-white shadow-lg",
  title: "text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent",
  description: "text-gray-600 font-medium",
  actionButton: "bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 shadow-lg hover:shadow-xl transition-all group font-semibold",
  standardButton: "h-11 bg-white border-2 border-gray-200 hover:border-blue-400 hover:bg-blue-50 transition-all duration-200 shadow-sm hover:shadow-md font-medium text-gray-700",
  tabButton: {
    base: "h-9 px-4 bg-white border-2 transition-all duration-200 shadow-sm font-medium",
    inactive: "border-gray-200 text-gray-700 hover:bg-blue-50 hover:border-blue-400",
    active: "border-blue-500 bg-blue-50 text-blue-700 hover:bg-blue-100",
  },
};

export const iconGradients = {
  feed: "bg-gradient-to-r from-blue-500 to-purple-500",
  challenge: "bg-gradient-to-r from-amber-500 to-orange-500",
  profile: "bg-gradient-to-r from-blue-600 to-purple-600",
  badge: "bg-gradient-to-r from-yellow-500 to-amber-500",
  streak: "bg-gradient-to-r from-orange-500 to-red-500",
  certification: "bg-gradient-to-r from-green-400 to-blue-500",
};

// Cards
export const cardStyles = {
  base: "border-2 shadow-xl bg-white overflow-hidden rounded-xl",
  headerGradient: "border-b p-6",
  hover:
    "bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow border border-gray-100 p-6", // Dashboard stats, etc.
  feed: "border-0 bg-white overflow-hidden mb-4 shadow-sm hover:shadow-md transition-shadow", // Feed items
};

// Buttons
export const buttonStyles = {
  back: "mb-6 hover:bg-blue-50 text-gray-700 font-medium",
  submit:
    "w-full h-12 shadow-lg hover:shadow-xl transition-all text-base font-bold text-white disabled:opacity-50 disabled:cursor-not-allowed",
  action:
    "inline-flex items-center gap-2 px-4 py-2 rounded-lg text-blue-600 hover:text-blue-700 hover:bg-blue-50 font-medium text-sm transition-all", // Dashboard 'more' buttons
};

// Inputs
export const inputStyles = {
  base: "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-base ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium file:text-foreground placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 md:text-sm",
  auth: "w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition-all",
};

// Navigation
export const navStyles = {
  link: "flex items-center gap-2 px-3 py-2 rounded-lg text-gray-700 hover:text-blue-600 hover:bg-blue-50 font-medium transition-all",
  activeLink: "bg-blue-50 text-blue-600",
  mobileLink: "flex items-center gap-3 px-3 py-3 rounded-lg text-gray-700 hover:text-blue-600 hover:bg-blue-50 font-medium transition-colors",
};

// Dropdowns
export const dropdownStyles = {
  wrapper:
    "absolute right-0 mt-2 w-48 bg-white border border-gray-200 rounded-lg shadow-lg z-50 overflow-hidden",
  item: "flex items-center gap-3 px-4 py-2.5 hover:bg-blue-50 text-gray-700 hover:text-blue-600 font-medium transition-colors w-full text-left",
  itemBordered: "border-b border-gray-100 last:border-0",
  header: "px-4 py-3 border-b border-gray-100 bg-gradient-to-r from-blue-50 to-purple-50",
  sectionHeader: "px-3 py-2 text-xs font-semibold text-gray-400 uppercase tracking-wider bg-gray-50",
};

// Calendar
export const calendarStyles = {
  day: "h-14 flex flex-col items-center justify-center border rounded-md text-sm relative transition-all cursor-pointer hover:border-blue-500",
  selected: "ring-2 ring-blue-500 ring-offset-2 z-10",
  hasEvent:
    "bg-green-100 dark:bg-green-900 border-green-300 dark:border-green-700 font-bold text-green-700 dark:text-green-300 shadow-sm",
  empty: "bg-white dark:bg-gray-800 border-gray-100 dark:border-gray-700 text-gray-400",
};

// Typography & Common Components
export const componentStyles = {
  sectionTitle: "text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2",
  avatar: {
    base: "bg-gradient-to-br from-blue-500 to-blue-600 rounded-full flex items-center justify-center text-white font-bold shadow-md transition-shadow",
    small: "w-9 h-9 text-sm", // Header
    medium: "w-10 h-10 text-sm", // Feed
    large: "w-12 h-12 text-lg", // Mobile menu/Profile small
    xlarge: "w-24 h-24 sm:w-28 sm:h-28 text-3xl", // Profile page
  },
};

// Badge Grade Colors
export const badgeGradeColors = {
  BRONZE:
    "text-amber-600 bg-amber-100 border-amber-200 dark:bg-amber-900/20 dark:border-amber-700/50 dark:text-amber-500",
  SILVER:
    "text-slate-500 bg-slate-100 border-slate-200 dark:bg-slate-800 dark:border-slate-700 dark:text-slate-400",
  GOLD: "text-yellow-600 bg-yellow-100 border-yellow-200 dark:bg-yellow-900/20 dark:border-yellow-700/50 dark:text-yellow-500",
  PLATINUM:
    "text-cyan-600 bg-cyan-100 border-cyan-200 dark:bg-cyan-900/20 dark:border-cyan-700/50 dark:text-cyan-400",
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
