# Button Standardization & Migration Guide

## 📊 Audit Summary

### Issues Identified

1. **Inconsistent Styling**: 27 files using custom gradient classNames instead of variants
2. **Raw HTML Buttons**: 3 files using `<button>` tags instead of UI Button component
3. **Accessibility Gaps**: Only 3/26 files have proper aria-label attributes
4. **Loading State Duplication**: Multiple implementations of loading spinners
5. **Scattered Constants**: Gradient styles defined in multiple locations

### Statistics
- **Total files with buttons**: 26
- **Files using UI Button component**: 26
- **Files with custom gradient className**: 27
- **Files with accessibility attributes**: 3
- **Raw HTML button usage**: 3 files (Header, ProfileHeader, BadgeItem)

---

## ✅ Solutions Implemented

### 1. Enhanced Button Component

**File**: `frontend/src/components/ui/button.tsx`

#### New Features
- **Built-in Loading State**: No need for custom spinner implementations
- **Brand Gradient Variants**: Pre-defined gradient styles
- **Accessibility**: Auto-managed `aria-disabled` attribute
- **Consistent Transitions**: All buttons use `transition-all`

#### New Variants Added
```typescript
variant="gradient"           // Primary brand gradient (blue-purple)
variant="gradientSuccess"    // Success gradient (green-teal)
variant="gradientWarning"    // Warning gradient (amber-orange)
variant="gradientInfo"       // Info gradient (blue-indigo)
variant="outlineGradient"    // Outlined with gradient border
variant="back"               // Back/cancel button style
```

#### New Sizes Added
```typescript
size="xl"    // Extra large (h-12, text-base)
```

#### New Props
```typescript
loading?: boolean    // Shows spinner and disables button
```

---

### 2. Button Style Constants

**File**: `frontend/src/constants/buttonStyles.ts`

Provides type-safe constants and presets for common button patterns:

```typescript
import { BUTTON_VARIANTS, BUTTON_SIZES, BUTTON_PRESETS } from "@/constants/buttonStyles";

// Use variants
<Button variant={BUTTON_VARIANTS.PRIMARY} />

// Use presets
<Button {...BUTTON_PRESETS.FORM_SUBMIT} loading={isLoading}>
  Submit
</Button>
```

---

## 🔄 Migration Examples

### Before vs After

#### Example 1: Landing Page CTA

**Before:**
```tsx
import { GRADIENT_PRIMARY, GRADIENT_PRIMARY_HOVER } from "@/constants/landing";

<Button
  size="lg"
  className={`${GRADIENT_PRIMARY} ${GRADIENT_PRIMARY_HOVER} text-white shadow-lg hover:shadow-xl`}
>
  무료로 시작하기
</Button>
```

**After:**
```tsx
<Button variant="gradient" size="lg" className="font-semibold">
  무료로 시작하기
</Button>
```

**Benefits:**
- ✅ 70% less code
- ✅ Type-safe variant
- ✅ Consistent shadow/transition
- ✅ Better maintainability

---

#### Example 2: Form Submit with Loading

**Before:**
```tsx
<Button
  type="submit"
  disabled={isPending}
  className={`${themeStyles.primary.btn} ${buttonStyles.submit}`}
>
  {isPending ? "제출 중..." : "제출"}
</Button>
```

**After:**
```tsx
<Button
  type="submit"
  variant="gradient"
  size="lg"
  loading={isPending}
>
  제출
</Button>
```

**Benefits:**
- ✅ Built-in loading spinner
- ✅ Auto-managed disabled state
- ✅ Cleaner code
- ✅ Consistent loading UX

---

#### Example 3: Raw Button to Component

**Before:**
```tsx
<button
  onClick={handleClick}
  className="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-700 hover:text-blue-600"
>
  Click me
</button>
```

**After:**
```tsx
<Button
  onClick={handleClick}
  variant="ghost"
  className="gap-3"
  aria-label="Descriptive action"
>
  Click me
</Button>
```

**Benefits:**
- ✅ Consistent styling
- ✅ Built-in accessibility (focus ring)
- ✅ Type-safe props
- ✅ Proper aria attributes

---

## 🎯 Migration Checklist

### For Each Button in Your Code:

- [ ] Replace custom gradient className with `variant="gradient"`
- [ ] Replace `disabled={isPending}` + custom loading with `loading={isPending}`
- [ ] Add `aria-label` for buttons without clear text
- [ ] Convert raw `<button>` tags to `<Button>` component
- [ ] Remove `themeStyles.*.btn` and use variants instead
- [ ] Remove custom spinner implementations
- [ ] Use `size="xl"` for hero/CTA buttons

---

## 📝 Variant Selection Guide

| Use Case | Variant | Size |
|----------|---------|------|
| Primary CTA | `gradient` | `lg` or `xl` |
| Form Submit | `gradient` | `lg` |
| Success Action | `gradientSuccess` | `lg` |
| Delete/Destroy | `destructive` | `default` |
| Cancel/Back | `outline` or `back` | `default` |
| Secondary Action | `outline` | `default` |
| Tertiary Action | `ghost` | `default` |
| Login Button | `outlineGradient` | `lg` |
| Icon Button | any | `icon` |

---

## ♿ Accessibility Requirements

### Every Button MUST Have:

1. **Clear Text or aria-label**
   ```tsx
   // Good: Has clear text
   <Button>Submit Form</Button>

   // Good: Icon button with aria-label
   <Button variant="ghost" size="icon" aria-label="Close dialog">
     <X />
   </Button>

   // Bad: Icon without label
   <Button size="icon"><X /></Button>
   ```

2. **Proper Disabled State**
   ```tsx
   // Good: Uses loading prop
   <Button loading={isPending}>Submit</Button>

   // Good: Has disabled state
   <Button disabled={!isValid}>Submit</Button>

   // Bad: No feedback when disabled
   <Button onClick={cannotClick ? undefined : handleClick}>Submit</Button>
   ```

3. **Focus Visible State** (built-in via `focus-visible:ring-2`)

---

## 🚀 Priority Files to Migrate

### High Priority (User-facing)
1. ✅ `components/landing/LandingPageClient.tsx` - COMPLETED
2. `components/auth/LoginForm.tsx`
3. `components/auth/SignUpForm.tsx`
4. `app/challenge/create/page.tsx`
5. `app/certification/create/page.tsx`

### Medium Priority (Internal pages)
6. `components/layout/Header.tsx` - Convert raw buttons
7. `components/profile/ProfileHeader.tsx` - Convert raw buttons
8. `components/follow/FollowButton.tsx`
9. `components/profile/AccountSettingsSection.tsx`

### Low Priority (Less frequent)
- Badge/Certification detail pages
- Settings pages
- Stats pages

---

## 📦 Files Modified

### Core Components
- ✅ `frontend/src/components/ui/button.tsx` - Enhanced Button component
- ✅ `frontend/src/constants/buttonStyles.ts` - New constants file

### Refactored Components
- ✅ `frontend/src/components/landing/LandingPageClient.tsx` - Standardized all buttons

---

## 🎨 Design System Alignment

### Brand Colors (via Variants)
- **Primary**: `gradient` - Blue to Purple (#2563eb → #9333ea)
- **Success**: `gradientSuccess` - Green to Teal (#059669 → #0d9488)
- **Warning**: `gradientWarning` - Amber to Orange (#f59e0b → #f97316)
- **Destructive**: `destructive` - Red tones

### Sizing Scale
- **sm**: 36px (h-9) - Compact actions
- **default**: 40px (h-10) - Standard buttons
- **lg**: 44px (h-11) - Forms, important actions
- **xl**: 48px (h-12) - Hero CTAs, primary landing

### Border Radius
- All buttons: `rounded-md` (0.375rem / 6px)
- Consistent across all sizes

### Shadows
- Gradient variants: `shadow-lg hover:shadow-xl`
- Outline variants: `shadow-md hover:shadow-lg`
- Ghost/Link: No shadow

---

## 🧪 Testing Checklist

After migration, verify:

- [ ] All buttons have proper hover states
- [ ] Focus rings are visible on keyboard navigation
- [ ] Loading states show spinner correctly
- [ ] Disabled buttons have reduced opacity
- [ ] Screen readers announce button purpose
- [ ] Color contrast ratio ≥ 4.5:1 (gradients pass)
- [ ] Buttons work on mobile (touch targets ≥ 44px)

---

## 🔗 Related Files

- `/frontend/src/components/ui/button.tsx` - Button component
- `/frontend/src/constants/buttonStyles.ts` - Style constants
- `/frontend/src/styles/common.ts` - Legacy theme styles (can be deprecated)
- `/frontend/src/constants/landing.ts` - Landing page constants

---

## 📞 Questions?

For questions about button usage, refer to:
1. This migration guide
2. `buttonStyles.ts` inline documentation
3. shadcn/ui Button documentation: https://ui.shadcn.com/docs/components/button
