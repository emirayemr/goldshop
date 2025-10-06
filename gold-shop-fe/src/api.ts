import axios from "axios";

/** -------- Types from backend -------- */
export type ProductView = {
    name: string;
    priceUsd: number;
    popularityOutOf5: number;
    images: { yellow: string; white: string; rose: string };
};

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

/** -------- Axios client -------- */
const BASE = (import.meta.env.VITE_API_BASE || "").replace(/\/+$/, "");

if (!BASE) {
    console.warn("VITE_API_BASE is not set. Define it in .env and Vercel Environment Variables.");
}

export const api = axios.create({
    baseURL: "https://goldshop-production.up.railway.app",
    timeout: 8000,
});

// basit hata mesajı
api.interceptors.response.use(
    (r) => r,
    (err) => {
        const msg = err?.response?.data?.message || err.message || "Network error";
        // eslint-disable-next-line no-console
        console.error("[API ERROR]", msg);
        return Promise.reject(err);
    }
);

/** -------- API calls -------- */
export async function fetchProducts(q: QueryParams): Promise<PageResponse<ProductView>> {
    const res = await api.get<PageResponse<ProductView>>("/api/products", { params: q });
    return res.data;
}
