import * as React from "react";

import { cn } from "@/lib/utils";
import { inputStyles } from "@/styles/common";

const Textarea = React.forwardRef<HTMLTextAreaElement, React.ComponentProps<"textarea">>(
  ({ className, ...props }, ref) => {
    return (
      <textarea className={cn(inputStyles.base, "min-h-[80px]", className)} ref={ref} {...props} />
    );
  }
);
Textarea.displayName = "Textarea";

export { Textarea };
