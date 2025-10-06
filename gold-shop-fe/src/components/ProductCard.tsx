import type { ProductView } from "../api";
import { fmtUsd } from "../lib/format";
import ImageCarousel from "./ImageCarousel";
import ColorSelector from "./ColorSelector";
import { useState } from "react";

type Color = "yellow" | "white" | "rose";

export default function ProductCard({ p }: { p: ProductView }) {
  const [color, setColor] = useState<Color>("yellow");

  return (
    <article className="rounded-2xl bg-[#121317] p-4 border border-white/10 shadow-lg shadow-black/30">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Carousel */}
        <ImageCarousel images={p.images} initial={color} />

        {/* Details */}
        <div className="flex flex-col gap-3">
          <h3 className="text-lg font-semibold">{p.name}</h3>

          <div className="text-xs text-neutral-400">
            Popularity: <span className="font-mono">{p.popularityOutOf5.toFixed(1)}/5</span>
          </div>

          <div className="text-2xl font-bold">{fmtUsd(p.priceUsd)}</div>

          <div className="mt-1">
            <div className="text-xs text-neutral-400 mb-1">Metal Color</div>
            <ColorSelector value={color} onChange={setColor} />
          </div>

          <div className="mt-auto flex gap-2">
            <button
              className="rounded-xl px-3 py-2 text-sm bg-emerald-500 hover:bg-emerald-400 text-black font-medium"
            >
              Add to cart
            </button>
            <button
              className="rounded-xl px-3 py-2 text-sm bg-transparent border border-white/10 hover:border-white/20"
            >
              Details
            </button>
          </div>
        </div>
      </div>
    </article>
  );
}
