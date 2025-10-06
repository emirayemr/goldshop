import { useQuery, keepPreviousData } from "@tanstack/react-query";
import { fetchProducts } from "../api";
import type { QueryParams } from "../api";
import { useEffect, useState } from "react";
import { parseNum } from "../lib/format";
import { useQueryState } from "../hooks/useQueryState";
import ProductCard from "../components/ProductCard";
import Empty from "../components/Empty";

function useDebounced<T>(value: T, ms = 400) {
  const [v, setV] = useState(value);
  useEffect(() => { const t = setTimeout(() => setV(value), ms); return () => clearTimeout(t); }, [value, ms]);
  return v;
}

export default function ProductsPage() {
  // URL <-> state codec
  const encode = (q: QueryParams) => {
    const sp = new URLSearchParams();
    if (q.minPrice !== undefined) sp.set("minPrice", String(q.minPrice));
    if (q.maxPrice !== undefined) sp.set("maxPrice", String(q.maxPrice));
    if (q.minPopularity !== undefined) sp.set("minPopularity", String(q.minPopularity));
    if (q.sortBy) sp.set("sortBy", q.sortBy);
    if (q.dir) sp.set("dir", q.dir);
    sp.set("page", String(q.page ?? 0));
    sp.set("size", String(q.size ?? 6));
    return sp;
  };
  const decode = (sp: URLSearchParams): Partial<QueryParams> => ({
    minPrice: parseNum(sp.get("minPrice") ?? undefined),
    maxPrice: parseNum(sp.get("maxPrice") ?? undefined),
    minPopularity: parseNum(sp.get("minPopularity") ?? undefined),
    sortBy: (sp.get("sortBy") as any) ?? "price",
    dir: (sp.get("dir") as any) ?? "asc",
    page: parseNum(sp.get("page") ?? undefined) ?? 0,
    size: parseNum(sp.get("size") ?? undefined) ?? 6,
  });

  const [query, setQuery] = useQueryState<QueryParams>(
    { sortBy: "price", dir: "asc", page: 0, size: 6 },
    encode,
    decode
  );

  const debounced = useDebounced(query, 350);

  const q = useQuery({
    queryKey: ["products", debounced],
    queryFn: () => fetchProducts(debounced),
    placeholderData: keepPreviousData,
    staleTime: 0,
  });

  const pageData = q.data ?? { items: [], total: 0 };
  const data = pageData.items;
  const total = pageData.total;

  const from = (query.page ?? 0) * (query.size ?? 6) + 1;
  const to = Math.min(from + (query.size ?? 6) - 1, total);

  return (
    <div className="flex flex-col gap-6">
      {/* Header + quick actions */}
      <div className="flex items-center gap-3">
        <h2 className="text-xl font-semibold">Products</h2>
        <div className="ml-auto flex items-center gap-2">
          {total > 0 && (
            <span className="text-xs text-neutral-400">{`Showing ${from}-${to} of ${total}`}</span>
          )}
          <button
            onClick={() => q.refetch()}
            className="rounded-xl px-3 py-1.5 text-sm bg-transparent border border-white/10 hover:border-white/20"
            title="Refresh"
          >
            Refresh
          </button>
        </div>
      </div>

      {/* FILTER BAR */}
      <div className="rounded-2xl bg-[#121317] p-4 border border-white/10">
        <div className="grid grid-cols-2 md:grid-cols-6 gap-3 items-end">
          <div>
            <label className="block text-xs text-neutral-400 mb-1">Min Price</label>
            <input
              type="number" min={0}
              value={query.minPrice ?? ""}
              onChange={(e) => setQuery((p) => ({ ...p, page: 0, minPrice: parseNum(e.target.value) }))}
              className="w-full rounded-xl bg-black/30 border border-white/10 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              placeholder="e.g. 300"
            />
          </div>
          <div>
            <label className="block text-xs text-neutral-400 mb-1">Max Price</label>
            <input
              type="number" min={0}
              value={query.maxPrice ?? ""}
              onChange={(e) => setQuery((p) => ({ ...p, page: 0, maxPrice: parseNum(e.target.value) }))}
              className="w-full rounded-xl bg-black/30 border border-white/10 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              placeholder="e.g. 600"
            />
          </div>
          <div>
            <label className="block text-xs text-neutral-400 mb-1">Min Popularity</label>
            <input
              type="number" step="0.1" min={0}
              value={query.minPopularity ?? ""}
              onChange={(e) => setQuery((p) => ({ ...p, page: 0, minPopularity: parseNum(e.target.value) }))}
              className="w-full rounded-xl bg-black/30 border border-white/10 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              placeholder="4.0"
            />
          </div>
          <div>
            <label className="block text-xs text-neutral-400 mb-1">Sort By</label>
            <select
              value={query.sortBy}
              onChange={(e) => setQuery((p) => ({ ...p, page: 0, sortBy: e.target.value as any }))}
              className="w-full rounded-xl bg-black/30 border border-white/10 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
            >
              <option value="price">price</option>
              <option value="popularity">popularity</option>
              <option value="name">name</option>
            </select>
          </div>
          <div>
            <label className="block text-xs text-neutral-400 mb-1">Direction</label>
            <select
              value={query.dir}
              onChange={(e) => setQuery((p) => ({ ...p, page: 0, dir: e.target.value as any }))}
              className="w-full rounded-xl bg-black/30 border border-white/10 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
            >
              <option value="asc">asc</option>
              <option value="desc">desc</option>
            </select>
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => setQuery({ sortBy: "price", dir: "asc", page: 0, size: query.size ?? 6 })}
              className="rounded-xl px-3 py-2 text-sm bg-transparent border border-white/10 hover:border-white/20"
            >
              Reset
            </button>
          </div>
        </div>
      </div>

      {/* LOADING SKELETON */}
      {q.isLoading && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="rounded-2xl bg-[#121317] p-4 border border-white/10 animate-pulse">
              <div className="w-full aspect-square rounded-lg bg-black/40 mb-3" />
              <div className="h-4 w-2/3 bg-black/40 rounded mb-2" />
              <div className="h-3 w-1/3 bg-black/40 rounded" />
            </div>
          ))}
        </div>
      )}

      {/* ERROR STATE + RETRY */}
      {!q.isLoading && q.isError && (
        <div className="rounded-2xl border border-white/10 bg-[#2a1313] p-4">
          <div className="text-red-300 text-sm mb-2">Failed to load products.</div>
          <button
            onClick={() => q.refetch()}
            className="rounded-xl px-3 py-2 text-sm bg-transparent border border-red-400/40 hover:border-red-300"
          >
            Try again
          </button>
        </div>
      )}

      {/* EMPTY STATE */}
      {!q.isLoading && !q.isError && data.length === 0 && (
        <Empty title="No products match your filters" hint="Try clearing filters or widening your range." />
      )}

      {/* LIST */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {data.map((p) => (
          <ProductCard key={p.name} p={p} />
        ))}
      </div>

      {/* PAGINATION */}
      {total > 0 && (
        <div className="flex items-center gap-3 justify-center mt-4">
          <button
            onClick={() => setQuery((q) => ({ ...q, page: Math.max(0, (q.page ?? 0) - 1) }))}
            disabled={(query.page ?? 0) === 0}
            className="px-3 py-1.5 text-sm rounded-lg bg-[#121317] border border-white/10 hover:border-white/20 disabled:opacity-40"
          >
            Prev
          </button>
          <span className="text-xs text-neutral-400">Page {(query.page ?? 0) + 1}</span>
          <button
            onClick={() => setQuery((q) => ({ ...q, page: (q.page ?? 0) + 1 }))}
            disabled={((query.page ?? 0) + 1) * (query.size ?? 6) >= total}
            className="px-3 py-1.5 text-sm rounded-lg bg-[#121317] border border-white/10 hover:border-white/20 disabled:opacity-40"
          >
            Next
          </button>
          <select
            value={String(query.size ?? 6)}
            onChange={(e) => setQuery((q) => ({ ...q, page: 0, size: Number(e.target.value) }))}
            className="rounded-xl bg-[#121317] border border-white/10 px-2 py-1.5 text-sm"
          >
            {[3, 6, 9, 12].map((n) => <option key={n} value={n}>{n}/page</option>)}
          </select>
        </div>
      )}
    </div>
  );
}
