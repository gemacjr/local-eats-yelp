import type { NextRequest } from "next/server";

// Same-origin proxy: browser -> Next.js -> Spring Boot. BACKEND_URL is read at request time,
// so one image works in docker-compose, locally, and on AWS.
const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

async function proxy(req: NextRequest, ctx: RouteContext<"/api/[...path]">) {
  const { path } = await ctx.params;
  const target = `${BACKEND_URL}/api/${path.map(encodeURIComponent).join("/")}${req.nextUrl.search}`;

  const headers = new Headers();
  for (const name of ["authorization", "content-type", "accept"]) {
    const value = req.headers.get(name);
    if (value) headers.set(name, value);
  }

  const hasBody = req.method !== "GET" && req.method !== "HEAD";
  const res = await fetch(target, {
    method: req.method,
    headers,
    body: hasBody ? await req.arrayBuffer() : undefined,
    cache: "no-store",
  });

  const out = new Headers();
  const contentType = res.headers.get("content-type");
  if (contentType) out.set("content-type", contentType);
  return new Response(res.body, { status: res.status, headers: out });
}

export { proxy as GET, proxy as POST, proxy as PUT, proxy as DELETE, proxy as PATCH };
