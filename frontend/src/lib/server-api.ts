import "server-only";

// Server Components talk to the backend directly (no browser, no CORS, no proxy hop).
const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export async function serverGet<T>(path: string): Promise<T | null> {
  const res = await fetch(`${BACKEND_URL}/api${path}`, { cache: "no-store" });
  if (res.status === 404) return null;
  if (!res.ok) throw new Error(`Backend ${res.status} for ${path}`);
  return res.json();
}
