/**
 * Standardized button style constants
 * Use these constants with the Button component's variant prop instead of custom classNames
 *
 * @example
 * ```tsx
 * import { Button } from "@/components/ui/button";
 *
 * // Instead of:
 * <Button className="bg-gradient-to-r from-blue-600 to-purple-600...">Submit</Button>
 *
 * // Use:
 * <Button variant="gradient" size="lg">Submit</Button>
 * ```
 */

/**
 * Button Variant Guide:
 *
 * PRIMARY ACTIONS:
 * - "gradient" - Primary brand gradient (blue-purple)
 * - "default" - Standard primary button
 *
 * SUCCESS ACTIONS:
 * - "gradientSuccess" - Success gradient (green-teal)
 *
 * WARNING/ALERT:
 * - "gradientWarning" - Warning gradient (amber-orange)
 * - "destructive" - Destructive/delete actions
 *
 * SECONDARY ACTIONS:
 * - "outline" - Secondary outlined button
 * - "outlineGradient" - Outlined with gradient border
 * - "secondary" - Secondary filled button
 * - "ghost" - Minimal ghost button
 *
 * NAVIGATION:
 * - "back" - Back/cancel button
 * - "link" - Text link style
 *
 * SIZES:
 * - "sm" - Small (h-9)
 * - "default" - Medium (h-10)
 * - "lg" - Large (h-11)
 * - "xl" - Extra large (h-12)
 * - "icon" - Icon only (10x10)
 */

export const BUTTON_VARIANTS = {
  // Primary
  PRIMARY: "gradient" as const,
  PRIMARY_OUTLINE: "outlineGradient" as const,

  // Success
  SUCCESS: "gradientSuccess" as const,

  // Warning
  WARNING: "gradientWarning" as const,
  DESTRUCTIVE: "destructive" as const,

  // Secondary
  SECONDARY: "secondary" as const,
  OUTLINE: "outline" as const,
  GHOST: "ghost" as const,

  // Utility
  BACK: "back" as const,
  LINK: "link" as const,
} as const;

export const BUTTON_SIZES = {
  SMALL: "sm" as const,
  MEDIUM: "default" as const,
  LARGE: "lg" as const,
  EXTRA_LARGE: "xl" as const,
  ICON: "icon" as const,
} as const;

/**
 * Common button combinations for specific use cases
 */
export const BUTTON_PRESETS = {
  // Form actions
  FORM_SUBMIT: { variant: BUTTON_VARIANTS.PRIMARY, size: BUTTON_SIZES.LARGE },
  FORM_SUBMIT_SUCCESS: { variant: BUTTON_VARIANTS.SUCCESS, size: BUTTON_SIZES.LARGE },
  FORM_CANCEL: { variant: BUTTON_VARIANTS.OUTLINE, size: BUTTON_SIZES.LARGE },

  // CTA buttons
  CTA_PRIMARY: { variant: BUTTON_VARIANTS.PRIMARY, size: BUTTON_SIZES.LARGE },
  CTA_SECONDARY: { variant: BUTTON_VARIANTS.PRIMARY_OUTLINE, size: BUTTON_SIZES.LARGE },

  // Navigation
  BACK_BUTTON: { variant: BUTTON_VARIANTS.BACK, size: BUTTON_SIZES.MEDIUM },

  // Destructive actions
  DELETE: { variant: BUTTON_VARIANTS.DESTRUCTIVE, size: BUTTON_SIZES.MEDIUM },
  DELETE_CONFIRM: { variant: BUTTON_VARIANTS.DESTRUCTIVE, size: BUTTON_SIZES.LARGE },
} as const;
