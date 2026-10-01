import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { PhotoSection } from "@/components/PhotoSection";
import { ReviewSection } from "@/components/ReviewSection";
import { Stars } from "@/components/Stars";
import { serverGet } from "@/lib/server-api";
import type { BusinessDetail } from "@/lib/types";

async function load(id: string) {
  if (!/^\d+$/.test(id)) notFound();
  const b = await serverGet<BusinessDetail>(`/businesses/${id}`);
  if (!b) notFound();
  return b;
}

export async function generateMetadata({ params }: PageProps<"/businesses/[id]">): Promise<Metadata> {
  const b = await load((await params).id);
  return { title: `${b.name} · Local Eats`, description: b.description ?? `${b.category} at ${b.address}` };
}

export default async function BusinessPage({ params }: PageProps<"/businesses/[id]">) {
  const b = await load((await params).id);
  const mapsUrl = `https://www.openstreetmap.org/?mlat=${b.latitude}&mlon=${b.longitude}#map=17/${b.latitude}/${b.longitude}`;

  return (
    <div className="space-y-10">
      <Link href="/" className="text-sm text-muted hover:text-foreground">← Back to search</Link>

      <header className="space-y-3">
        <span className="rounded-full bg-accent-soft px-2.5 py-1 text-xs font-medium text-accent">{b.category}</span>
        <h1 className="font-display text-4xl font-semibold tracking-tight">{b.name}</h1>
        <div className="flex flex-wrap items-center gap-2">
          <Stars value={b.avgRating} size={22} />
          <span className="text-muted">
            {b.numRatings > 0 ? `${b.avgRating.toFixed(1)} from ${b.numRatings} review${b.numRatings === 1 ? "" : "s"}` : "No reviews yet"}
          </span>
        </div>
        {b.description && <p className="max-w-2xl leading-relaxed">{b.description}</p>}
        <dl className="card grid gap-3 p-4 text-sm sm:grid-cols-3">
          <div>
            <dt className="text-muted">Address</dt>
            <dd>
              <a href={mapsUrl} target="_blank" rel="noreferrer" className="hover:text-accent hover:underline">{b.address}</a>
            </dd>
          </div>
          {b.phone && (
            <div>
              <dt className="text-muted">Phone</dt>
              <dd>{b.phone}</dd>
            </div>
          )}
          {b.website && (
            <div>
              <dt className="text-muted">Website</dt>
              <dd className="truncate">
                <a href={b.website} target="_blank" rel="noreferrer" className="hover:text-accent hover:underline">{b.website}</a>
              </dd>
            </div>
          )}
        </dl>
      </header>

      <PhotoSection businessId={b.id} initialPhotos={b.photos} />
      <ReviewSection businessId={b.id} initialReviews={b.recentReviews} initialHasMore={b.hasMoreReviews} />
    </div>
  );
}
