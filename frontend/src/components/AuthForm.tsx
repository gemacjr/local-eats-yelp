"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuth } from "./AuthProvider";

export function AuthForm({ mode }: { mode: "login" | "register" }) {
  const { login, register } = useAuth();
  const router = useRouter();
  const next = useSearchParams().get("next");
  const [email, setEmail] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      if (mode === "login") await login(email, password);
      else await register(email, displayName, password);
      // Only follow same-site relative paths to avoid open redirects.
      router.push(next && next.startsWith("/") && !next.startsWith("//") ? next : "/");
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setBusy(false);
    }
  }

  const isLogin = mode === "login";
  return (
    <div className="mx-auto max-w-sm">
      <h1 className="font-display text-3xl font-semibold">{isLogin ? "Welcome back" : "Create an account"}</h1>
      <form onSubmit={onSubmit} className="card mt-6 space-y-3 p-5">
        {!isLogin && (
          <input className="input" placeholder="Display name" required maxLength={100} value={displayName} onChange={(e) => setDisplayName(e.target.value)} />
        )}
        <input className="input" type="email" placeholder="Email" required value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" />
        <input
          className="input"
          type="password"
          placeholder={isLogin ? "Password" : "Password (8+ characters)"}
          required
          minLength={isLogin ? undefined : 8}
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          autoComplete={isLogin ? "current-password" : "new-password"}
        />
        {error && <p className="text-sm text-accent">{error}</p>}
        <button className="btn w-full" disabled={busy}>{busy ? "Please wait…" : isLogin ? "Log in" : "Sign up"}</button>
      </form>
      <p className="mt-4 text-center text-sm text-muted">
        {isLogin ? "New here? " : "Already have an account? "}
        <Link href={isLogin ? "/register" : "/login"} className="font-medium text-accent hover:underline">
          {isLogin ? "Create an account" : "Log in"}
        </Link>
      </p>
    </div>
  );
}
