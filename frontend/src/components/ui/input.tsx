import * as React from "react";

import { cn } from "@/lib/utils";
import { inputStyles } from "@/styles/common";

const Input = React.forwardRef<HTMLInputElement, React.ComponentProps<"input">>(
  ({ className, type, ...props }, ref) => {
    return <input type={type} className={cn(inputStyles.base, className)} ref={ref} {...props} />;
  }
);
Input.displayName = "Input";

export { Input };
