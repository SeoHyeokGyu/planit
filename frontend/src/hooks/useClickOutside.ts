import { useEffect, RefObject } from "react";

/**
 * Hook that handles clicks outside of the passed ref
 * @param ref - React ref object for the element to detect outside clicks
 * @param handler - Callback function to execute when clicking outside
 * @param isActive - Optional flag to enable/disable the listener (default: true)
 */
export function useClickOutside<T extends HTMLElement = HTMLElement>(
  ref: RefObject<T | null>,
  handler: (event: MouseEvent) => void,
  isActive: boolean = true
) {
  useEffect(() => {
    if (!isActive) return;

    const handleClickOutside = (event: MouseEvent) => {
      if (ref.current && !ref.current.contains(event.target as Node)) {
        handler(event);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [ref, handler, isActive]);
}
