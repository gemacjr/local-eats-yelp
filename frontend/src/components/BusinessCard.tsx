import Link from "next/link";
import { formatDistance } from "@/lib/format";
import type { BusinessSummary } from "@/lib/types";
import { Stars } from "./Stars";

export function BusinessCard({ b, rank }: { b: BusinessSummary; rank: number }) {
  const distance = formatDistance(b.distanceMeters);
  return (
    <Link href={`/businesses/${b.id}`} className="card group block p-4 transition hover:border-foreground/20 hover:shadow-sm">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <h3 className="font-display text-lg font-semibold group-hover:text-accent">
            {rank}. {b.name}
          </h3>
          <div className="mt-1 flex flex-wrap items-center gap-2 text-sm">
            <Stars value={b.avgRating} />
            <span className="text-muted">
              {b.numRatings > 0 ? `${b.avgRating.toFixed(1)} · ${b.numRatings} review${b.numRatings === 1 ? "" : "s"}` : "No reviews yet"}
            </span>
          </div>
          <p className="mt-2 truncate text-sm text-muted">{b.address}</p>
        </div>
        <div className="flex shrink-0 flex-col items-end gap-2 text-xs">
          <span className="rounded-full bg-accent-soft px-2.5 py-1 font-medium text-accent">{b.category}</span>
          {distance && <span className="text-muted">{distance}</span>}
        </div>
      </div>
    </Link>
  );
}
