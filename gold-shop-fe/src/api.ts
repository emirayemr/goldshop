import axios from "axios";

export type ProductView = {
  name: string;
  priceUsd: number;
  popularityOutOf5: number;
  images: { yellow: string; white: string; rose: string };
};

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE ?? "http://localhost:8080",
});

export type QueryParams = {
  minPrice?: number;
  maxPrice?: number;
  minPopularity?: number;
  sortBy?: "price" | "popularity" | "name";
  dir?: "asc" | "desc";
  page?: number; // 0-based
  size?: number;
};

export type PageResponse<T> = { items: T[]; total: number };

export async function fetchProducts(q: QueryParams) {
  const res = await api.get<PageResponse<ProductView>>("/api/products", { params: q });
  return res.data;
}



