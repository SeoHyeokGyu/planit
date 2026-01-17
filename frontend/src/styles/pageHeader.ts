/**
 * Shared styles for consistent page headers across the application
 */

export const pageHeaderStyles = {
  container: "mb-8",

  // Main wrapper
  wrapper: "flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4",

  // Title section
  titleSection: "flex-1",
  titleWrapper: "flex items-center gap-3 mb-3",

  // Icon styles (use with specific gradient colors)
  iconBase: "w-10 h-10 rounded-lg flex items-center justify-center text-white shadow-lg",

  // Title and description
  title:
    "text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent",
  description: "text-gray-600 font-medium",

  // Action button (gradient style for primary actions)
  actionButton:
    "bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 shadow-lg hover:shadow-xl transition-all group font-semibold",

  // Standard action buttons and controls (dropdowns, tabs, etc.)
  standardButton:
    "h-11 bg-white border-2 border-gray-200 hover:border-blue-400 hover:bg-blue-50 transition-all duration-200 shadow-sm hover:shadow-md font-medium text-gray-700",

  // Tab button styles
  tabButton: {
    base: "h-9 px-4 bg-white border-2 transition-all duration-200 shadow-sm font-medium",
    inactive: "border-gray-200 text-gray-700 hover:bg-blue-50 hover:border-blue-400",
    active: "border-blue-500 bg-blue-50 text-blue-700 hover:bg-blue-100",
  },
} as const;

/**
 * Icon background gradients for different page types
 */
export const iconGradients = {
  feed: "bg-gradient-to-r from-blue-500 to-purple-500",
  challenge: "bg-gradient-to-r from-amber-500 to-orange-500",
  profile: "bg-gradient-to-r from-blue-600 to-purple-600",
  badge: "bg-gradient-to-r from-yellow-500 to-amber-500",
  streak: "bg-gradient-to-r from-orange-500 to-red-500",
  certification: "bg-gradient-to-r from-green-400 to-blue-500",
} as const;
