"use client";

import { useState } from "react";

const STAR = "M10 1.5l2.6 5.3 5.9.9-4.3 4.1 1 5.8L10 14.9l-5.2 2.7 1-5.8L1.5 7.7l5.9-.9z";

/** Read-only star display; supports fractional averages via a clipped overlay. */
export function Stars({ value, size = 16 }: { value: number; size?: number }) {
  return (
    <span className="inline-flex" aria-label={`${value.toFixed(1)} out of 5 stars`} role="img">
      {[0, 1, 2, 3, 4].map((i) => {
        const fill = Math.max(0, Math.min(1, value - i));
        return (
          <svg key={i} width={size} height={size} viewBox="0 0 20 20" className="shrink-0">
            <path d={STAR} className="fill-border" />
            <path d={STAR} className="fill-star" style={{ clipPath: `inset(0 ${100 - fill * 100}% 0 0)` }} />
          </svg>
        );
      })}
    </span>
  );
}

/** Interactive 1–5 picker used by the review form. */
export function StarPicker({ value, onChange }: { value: number; onChange: (v: number) => void }) {
  const [hover, setHover] = useState(0);
  const shown = hover || value;
  return (
    <div className="flex gap-1" onMouseLeave={() => setHover(0)} role="radiogroup" aria-label="Rating">
      {[1, 2, 3, 4, 5].map((n) => (
        <button
          key={n}
          type="button"
          role="radio"
          aria-checked={value === n}
          aria-label={`${n} star${n > 1 ? "s" : ""}`}
          onMouseEnter={() => setHover(n)}
          onClick={() => onChange(n)}
          className="rounded p-0.5 transition hover:scale-110"
        >
          <svg width={28} height={28} viewBox="0 0 20 20">
            <path d={STAR} className={n <= shown ? "fill-star" : "fill-border"} />
          </svg>
        </button>
      ))}
    </div>
  );
}
