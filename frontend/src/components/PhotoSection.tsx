"use client";

import { useRef, useState } from "react";
import { api } from "@/lib/api";
import type { Photo } from "@/lib/types";
import { useAuth } from "./AuthProvider";

interface UploadUrl {
  uploadUrl: string;
  key: string;
  contentType: string;
}

/**
 * Three-step direct-to-S3 upload: ask the API for a presigned PUT URL, PUT the bytes
 * straight to S3/LocalStack, then confirm the key so the API records the photo.
 */
export function PhotoSection({ businessId, initialPhotos }: { businessId: number; initialPhotos: Photo[] }) {
  const { user } = useAuth();
  const [photos, setPhotos] = useState(initialPhotos);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const fileRef = useRef<HTMLInputElement>(null);

  async function upload(file: File) {
    setBusy(true);
    setError(null);
    try {
      const target = await api<UploadUrl>(`/businesses/${businessId}/photos/upload-url`, {
        method: "POST",
        json: { contentType: file.type },
      });
      const put = await fetch(target.uploadUrl, { method: "PUT", body: file, headers: { "Content-Type": target.contentType } });
      if (!put.ok) throw new Error(`Upload to storage failed (${put.status})`);
      const photo = await api<Photo>(`/businesses/${businessId}/photos`, { method: "POST", json: { key: target.key } });
      setPhotos((ps) => [photo, ...ps]);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
      if (fileRef.current) fileRef.current.value = "";
    }
  }

  return (
    <section className="space-y-3">
      <div className="flex items-center justify-between">
        <h2 className="font-display text-2xl font-semibold">Photos</h2>
        {user && (
          <label className={`btn-ghost cursor-pointer ${busy ? "pointer-events-none opacity-50" : ""}`}>
            {busy ? "Uploading…" : "Add photo"}
            <input
              ref={fileRef}
              type="file"
              accept="image/jpeg,image/png,image/webp,image/gif"
              className="sr-only"
              onChange={(e) => e.target.files?.[0] && upload(e.target.files[0])}
            />
          </label>
        )}
      </div>
      {error && <p className="text-sm text-accent">{error}</p>}
      {photos.length === 0 ? (
        <p className="text-sm text-muted">No photos yet.</p>
      ) : (
        <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
          {photos.map((p) => (
            // Presigned S3 URLs are short-lived and per-request, so next/image caching doesn't apply.
            // eslint-disable-next-line @next/next/no-img-element
            <img key={p.id} src={p.url} alt={p.caption ?? "Business photo"} className="aspect-square w-full rounded-lg object-cover" />
          ))}
        </div>
      )}
    </section>
  );
}
