export type SortOrder = "RELEVANCE" | "RATING" | "DISTANCE";

export interface Page<T> {
  items: T[];
  page: number;
  size: number;
  hasMore: boolean;
}

export interface BusinessSummary {
  id: number;
  name: string;
  category: string;
  address: string;
  latitude: number;
  longitude: number;
  avgRating: number;
  numRatings: number;
  distanceMeters: number | null;
}

export interface Review {
  id: number;
  businessId: number;
  userId: number;
  authorName: string;
  rating: number;
  text: string | null;
  createdAt: string;
}

export interface Photo {
  id: number;
  url: string;
  caption: string | null;
  createdAt: string;
}

export interface BusinessDetail {
  id: number;
  name: string;
  description: string | null;
  category: string;
  address: string;
  phone: string | null;
  website: string | null;
  latitude: number;
  longitude: number;
  avgRating: number;
  numRatings: number;
  recentReviews: Review[];
  hasMoreReviews: boolean;
  photos: Photo[];
}

export interface Location {
  id: number;
  name: string;
  slug: string;
  type: "CITY" | "NEIGHBORHOOD";
}

export interface User {
  id: number;
  email: string;
  displayName: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}
