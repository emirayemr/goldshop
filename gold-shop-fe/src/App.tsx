import ProductsPage from "./pages/ProductsPage";

export default function App() {
  return (
    <div className="min-h-dvh">
      <header className="sticky top-0 z-10 border-b border-white/10 bg-black/40 backdrop-blur">
        <div className="mx-auto max-w-6xl px-4 py-3 flex items-center gap-2">
          <div className="size-2 rounded-full bg-emerald-400 animate-pulse" />
          <h1 className="text-lg font-semibold">Gold Shop</h1>
          <span className="ml-auto text-xs text-neutral-400">
            price = (popularityScore + 1) × weight × goldPrice
          </span>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-6">
        <ProductsPage />
      </main>
    </div>
  );
}
