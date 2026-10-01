"use client";

import Link from "next/link";
import { useAuth } from "./AuthProvider";

export function NavBar() {
  const { user, ready, logout } = useAuth();
  return (
    <header className="border-b border-border bg-surface">
      <nav className="mx-auto flex max-w-5xl items-center justify-between gap-4 px-4 py-3">
        <Link href="/" className="font-display text-xl font-semibold tracking-tight">
          <span className="text-accent">Local</span> Eats
        </Link>
        <div className="flex items-center gap-3 text-sm">
          {ready && user ? (
            <>
              <Link href="/businesses/new" className="hidden text-muted hover:text-foreground sm:inline">
                Add a business
              </Link>
              <span className="text-muted">Hi, {user.displayName}</span>
              <button onClick={logout} className="btn-ghost py-1.5">
                Log out
              </button>
            </>
          ) : ready ? (
            <>
              <Link href="/login" className="text-muted hover:text-foreground">
                Log in
              </Link>
              <Link href="/register" className="btn py-1.5">
                Sign up
              </Link>
            </>
          ) : null}
        </div>
      </nav>
    </header>
  );
}
