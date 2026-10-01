"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/components/AuthProvider";
import { api } from "@/lib/api";
import type { BusinessDetail } from "@/lib/types";

const EMPTY = { name: "", category: "", address: "", description: "", phone: "", website: "", latitude: "", longitude: "" };

export default function NewBusinessPage() {
  const { user, ready } = useAuth();
  const router = useRouter();
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const set = (k: keyof typeof EMPTY) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
    setForm((f) => ({ ...f, [k]: e.target.value }));

  function useMyLocation() {
    navigator.geolocation.getCurrentPosition(
      (p) => setForm((f) => ({ ...f, latitude: p.coords.latitude.toFixed(6), longitude: p.coords.longitude.toFixed(6) })),
      () => setError("Couldn't read your location."),
    );
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const created = await api<BusinessDetail>("/businesses", {
        method: "POST",
        json: {
          ...form,
          description: form.description || null,
          phone: form.phone || null,
          website: form.website || null,
          latitude: Number(form.latitude),
          longitude: Number(form.longitude),
        },
      });
      router.push(`/businesses/${created.id}`);
    } catch (err) {
      setError((err as Error).message);
      setBusy(false);
    }
  }

  if (!ready) return null;
  if (!user)
    return (
      <p className="card p-6 text-center">
        <Link href="/login?next=/businesses/new" className="font-medium text-accent hover:underline">Log in</Link> to add a business.
      </p>
    );

  return (
    <div className="mx-auto max-w-xl">
      <h1 className="font-display text-3xl font-semibold">Add a business</h1>
      <p className="mt-1 text-sm text-muted">It&apos;s automatically tagged with every city and neighborhood whose boundary contains its coordinates.</p>
      <form onSubmit={onSubmit} className="card mt-6 grid gap-3 p-5 sm:grid-cols-2">
        <input className="input sm:col-span-2" placeholder="Name *" required value={form.name} onChange={set("name")} />
        <input className="input" placeholder="Category * (e.g. Coffee)" required value={form.category} onChange={set("category")} />
        <input className="input" placeholder="Phone" value={form.phone} onChange={set("phone")} />
        <input className="input sm:col-span-2" placeholder="Address *" required value={form.address} onChange={set("address")} />
        <input className="input" placeholder="Latitude *" required inputMode="decimal" value={form.latitude} onChange={set("latitude")} />
        <input className="input" placeholder="Longitude *" required inputMode="decimal" value={form.longitude} onChange={set("longitude")} />
        <button type="button" className="btn-ghost sm:col-span-2" onClick={useMyLocation}>📍 Use my current location</button>
        <input className="input sm:col-span-2" placeholder="Website" type="url" value={form.website} onChange={set("website")} />
        <textarea className="input min-h-24 sm:col-span-2" placeholder="Description" value={form.description} onChange={set("description")} />
        {error && <p className="text-sm text-accent sm:col-span-2">{error}</p>}
        <button className="btn sm:col-span-2" disabled={busy}>{busy ? "Saving…" : "Create business"}</button>
      </form>
    </div>
  );
}
