"use client";

import { createContext, useCallback, useContext, useEffect, useState } from "react";
import { api, getToken, setToken } from "@/lib/api";
import type { AuthResponse, User } from "@/lib/types";

interface AuthState {
  user: User | null;
  ready: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, displayName: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    // Resolve the stored token (if any) to a user; state is only set from async callbacks.
    (getToken() ? api<User>("/auth/me") : Promise.resolve(null))
      .then(setUser)
      .catch(() => setToken(null))
      .finally(() => setReady(true));
  }, []);

  const accept = useCallback((res: AuthResponse) => {
    setToken(res.token);
    setUser(res.user);
  }, []);

  const login = useCallback(
    async (email: string, password: string) =>
      accept(await api<AuthResponse>("/auth/login", { method: "POST", json: { email, password } })),
    [accept],
  );

  const register = useCallback(
    async (email: string, displayName: string, password: string) =>
      accept(await api<AuthResponse>("/auth/register", { method: "POST", json: { email, displayName, password } })),
    [accept],
  );

  const logout = useCallback(() => {
    setToken(null);
    setUser(null);
  }, []);

  return <AuthContext.Provider value={{ user, ready, login, register, logout }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside <AuthProvider>");
  return ctx;
}
