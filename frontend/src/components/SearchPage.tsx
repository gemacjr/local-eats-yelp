"use client";

import { useEffect, useState } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { api } from "@/lib/api";
import type { BusinessSummary, Location, Page, SortOrder } from "@/lib/types";
import { BusinessCard } from "./BusinessCard";

const PAGE_SIZE = 10;
const NEAR_ME = "__near_me__";

/**
 * Search state lives in the URL (?query=&category=&location=&lat=&lng=&radius=&sort=&page=),
 * so results are shareable and the back button works.
 */
export function SearchPage() {
  const router = useRouter();
  const pathname = usePathname();
  const params = useSearchParams();

  const [locations, setLocations] = useState<Location[]>([]);
  const [categories, setCategories] = useState<string[]>([]);
  // `forQs` records which URL the results belong to; loading is derived rather than stored.
  const [fetched, setFetched] = useState<{ forQs: string | null; results: Page<BusinessSummary> | null; error: string | null }>(
    { forQs: null, results: null, error: null },
  );
  const [geoError, setGeoError] = useState<string | null>(null);

  // Form fields, seeded from the URL.
  const [query, setQuery] = useState(params.get("query") ?? "");
  const [category, setCategory] = useState(params.get("category") ?? "");
  const [where, setWhere] = useState(params.get("lat") ? NEAR_ME : (params.get("location") ?? ""));
  const [radius, setRadius] = useState(params.get("radius") ?? "5000");
  const [sort, setSort] = useState<SortOrder>((params.get("sort") as SortOrder) ?? "RELEVANCE");

  useEffect(() => {
    api<Location[]>("/locations").then(setLocations).catch(() => {});
    api<string[]>("/categories").then(setCategories).catch(() => {});
  }, []);

  // Run the search whenever the URL changes.
  const qs = params.toString();
  useEffect(() => {
    let cancelled = false;
    const search = new URLSearchParams(qs);
    search.set("size", String(PAGE_SIZE));
    api<Page<BusinessSummary>>(`/businesses?${search}`)
      .then((results) => !cancelled && setFetched({ forQs: qs, results, error: null }))
      .catch((e: Error) => !cancelled && setFetched((f) => ({ forQs: qs, results: f.results, error: e.message })));
    return () => {
      cancelled = true;
    };
  }, [qs]);

  const { results } = fetched;
  const loading = fetched.forQs !== qs;
  const error = geoError ?? fetched.error;

  function navigate(next: Record<string, string | undefined>) {
    const sp = new URLSearchParams();
    for (const [k, v] of Object.entries(next)) if (v) sp.set(k, v);
    router.push(`${pathname}?${sp}`);
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setGeoError(null);
    const base = { query: query.trim() || undefined, category: category || undefined, sort: sort === "RELEVANCE" ? undefined : sort };
    if (where === NEAR_ME) {
      try {
        const pos = await new Promise<GeolocationPosition>((resolve, reject) =>
          navigator.geolocation.getCurrentPosition(resolve, reject, { timeout: 10000 }),
        );
        navigate({ ...base, lat: pos.coords.latitude.toFixed(5), lng: pos.coords.longitude.toFixed(5), radius });
      } catch {
        setGeoError("Couldn't get your location. Allow location access or pick a city instead.");
      }
      return;
    }
    navigate({ ...base, location: where || undefined });
  }

  function goToPage(page: number) {
    const sp = new URLSearchParams(qs);
    if (page > 0) sp.set("page", String(page));
    else sp.delete("page");
    router.push(`${pathname}?${sp}`);
  }

  const page = results?.page ?? 0;
  const cities = locations.filter((l) => l.type === "CITY");
  const hoods = locations.filter((l) => l.type === "NEIGHBORHOOD");

  return (
    <div className="space-y-6">
      <section>
        <h1 className="font-display text-3xl font-semibold tracking-tight sm:text-4xl">Find something good nearby.</h1>
        <p className="mt-2 text-muted">Search by name, category, a neighborhood, or your current location.</p>
      </section>

      <form onSubmit={onSubmit} className="card grid gap-3 p-4 sm:grid-cols-12">
        <input
          className="input sm:col-span-5"
          placeholder="Tacos, coffee, “Tartine”…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          aria-label="Search"
        />
        <select className="input sm:col-span-4" value={where} onChange={(e) => setWhere(e.target.value)} aria-label="Where">
          <option value="">Anywhere</option>
          <option value={NEAR_ME}>📍 Near me</option>
          <optgroup label="Cities">
            {cities.map((l) => (
              <option key={l.slug} value={l.slug}>{l.name}</option>
            ))}
          </optgroup>
          <optgroup label="Neighborhoods">
            {hoods.map((l) => (
              <option key={l.slug} value={l.slug}>{l.name}</option>
            ))}
          </optgroup>
        </select>
        <button className="btn sm:col-span-3" type="submit">Search</button>

        <select className="input sm:col-span-4" value={category} onChange={(e) => setCategory(e.target.value)} aria-label="Category">
          <option value="">All categories</option>
          {categories.map((c) => (
            <option key={c} value={c}>{c}</option>
          ))}
        </select>
        <select className="input sm:col-span-4" value={sort} onChange={(e) => setSort(e.target.value as SortOrder)} aria-label="Sort">
          <option value="RELEVANCE">Sort: best match</option>
          <option value="RATING">Sort: highest rated</option>
          <option value="DISTANCE">Sort: closest (near me)</option>
        </select>
        <select
          className="input sm:col-span-4"
          value={radius}
          onChange={(e) => setRadius(e.target.value)}
          disabled={where !== NEAR_ME}
          aria-label="Radius"
        >
          <option value="1000">Within 1 km</option>
          <option value="5000">Within 5 km</option>
          <option value="15000">Within 15 km</option>
          <option value="50000">Within 50 km</option>
        </select>
      </form>

      {error && <p className="rounded-lg bg-accent-soft px-4 py-3 text-sm text-accent">{error}</p>}

      <section aria-busy={loading} className={loading ? "opacity-60 transition" : "transition"}>
        {results && results.items.length === 0 && !loading && (
          <p className="card p-8 text-center text-muted">No businesses match. Try a wider area or fewer filters.</p>
        )}
        <div className="grid gap-3">
          {results?.items.map((b, i) => (
            <BusinessCard key={b.id} b={b} rank={page * PAGE_SIZE + i + 1} />
          ))}
        </div>
        {results && (page > 0 || results.hasMore) && (
          <div className="mt-6 flex items-center justify-between">
            <button className="btn-ghost" disabled={page === 0} onClick={() => goToPage(page - 1)}>← Previous</button>
            <span className="text-sm text-muted">Page {page + 1}</span>
            <button className="btn-ghost" disabled={!results.hasMore} onClick={() => goToPage(page + 1)}>Next →</button>
          </div>
        )}
      </section>
    </div>
  );
}
