"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { api, ApiError } from "@/lib/api";
import { formatDate } from "@/lib/format";
import type { Page, Review } from "@/lib/types";
import { useAuth } from "./AuthProvider";
import { StarPicker, Stars } from "./Stars";

const PAGE_SIZE = 10;

export function ReviewSection({
  businessId,
  initialReviews,
  initialHasMore,
}: {
  businessId: number;
  initialReviews: Review[];
  initialHasMore: boolean;
}) {
  const { user, ready } = useAuth();
  const router = useRouter();
  const [reviews, setReviews] = useState(initialReviews);
  const [hasMore, setHasMore] = useState(initialHasMore);
  const [page, setPage] = useState(0);
  const [rating, setRating] = useState(0);
  const [text, setText] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const alreadyReviewed = !!user && reviews.some((r) => r.userId === user.id);

  async function loadMore() {
    const next = await api<Page<Review>>(`/businesses/${businessId}/reviews?page=${page + 1}&size=${PAGE_SIZE}`);
    setReviews((rs) => [...rs, ...next.items.filter((n) => !rs.some((r) => r.id === n.id))]);
    setHasMore(next.hasMore);
    setPage(next.page);
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!rating) return setError("Pick a star rating.");
    setSubmitting(true);
    setError(null);
    try {
      const created = await api<Review>(`/businesses/${businessId}/reviews`, {
        method: "POST",
        json: { rating, text: text.trim() || null },
      });
      setReviews((rs) => [created, ...rs]);
      setRating(0);
      setText("");
      router.refresh(); // re-render the server header with the new average
    } catch (err) {
      setError(err instanceof ApiError && err.status === 409 ? "You've already reviewed this business." : (err as Error).message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="space-y-4">
      <h2 className="font-display text-2xl font-semibold">Reviews</h2>

      {!ready ? null : !user ? (
        <p className="card p-4 text-sm text-muted">
          <Link href={`/login?next=/businesses/${businessId}`} className="font-medium text-accent hover:underline">Log in</Link> to write a review.
        </p>
      ) : alreadyReviewed ? (
        <p className="card p-4 text-sm text-muted">Thanks, you&apos;ve reviewed this business. Each person gets one review per business.</p>
      ) : (
        <form onSubmit={submit} className="card space-y-3 p-4">
          <StarPicker value={rating} onChange={setRating} />
          <textarea
            className="input min-h-24"
            placeholder="What stood out? (optional)"
            maxLength={5000}
            value={text}
            onChange={(e) => setText(e.target.value)}
          />
          {error && <p className="text-sm text-accent">{error}</p>}
          <button className="btn" disabled={submitting || !rating}>
            {submitting ? "Posting…" : "Post review"}
          </button>
        </form>
      )}

      {reviews.length === 0 ? (
        <p className="text-sm text-muted">No reviews yet. Be the first.</p>
      ) : (
        <ul className="divide-y divide-border">
          {reviews.map((r) => (
            <li key={r.id} className="py-4">
              <div className="flex items-center justify-between gap-2">
                <span className="font-medium">{r.authorName}</span>
                <span className="text-xs text-muted">{formatDate(r.createdAt)}</span>
              </div>
              <Stars value={r.rating} size={14} />
              {r.text && <p className="mt-2 whitespace-pre-line text-sm leading-relaxed">{r.text}</p>}
            </li>
          ))}
        </ul>
      )}
      {hasMore && (
        <button className="btn-ghost w-full" onClick={loadMore}>Show more reviews</button>
      )}
    </section>
  );
}
