import type { Metadata } from "next";
import { Fraunces, Geist } from "next/font/google";
import { AuthProvider } from "@/components/AuthProvider";
import { NavBar } from "@/components/NavBar";
import "./globals.css";

const geistSans = Geist({ variable: "--font-geist-sans", subsets: ["latin"] });
const fraunces = Fraunces({ variable: "--font-fraunces", subsets: ["latin"] });

export const metadata: Metadata = {
  title: "Local Eats",
  description: "Search local businesses, read reviews, and share your own.",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="en" className={`${geistSans.variable} ${fraunces.variable} h-full antialiased`}>
      <body className="flex min-h-full flex-col font-sans">
        <AuthProvider>
          <NavBar />
          <main className="mx-auto w-full max-w-5xl flex-1 px-4 py-8">{children}</main>
          <footer className="border-t border-border py-6 text-center text-xs text-muted">
            Built from the Hello Interview Yelp system design breakdown · Next.js + Spring Boot + PostGIS
          </footer>
        </AuthProvider>
      </body>
    </html>
  );
}
