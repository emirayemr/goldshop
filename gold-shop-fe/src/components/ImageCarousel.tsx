import { useState } from "react";

type Color = "yellow" | "white" | "rose";

export default function ImageCarousel({
  images,
  initial = "yellow",
}: {
  images: { yellow: string; white: string; rose: string };
  initial?: Color;
}) {
  const [color, setColor] = useState<Color>(initial);
  const src = images[color];

  return (
    <div>
      <div className="aspect-square overflow-hidden rounded-xl bg-black/30">
        <img
          src={src}
          alt={`${color} gold`}
          className="size-full object-cover"
          loading="lazy"
        />
      </div>

      <div className="mt-2 grid grid-cols-3 gap-2">
        {(["yellow", "white", "rose"] as const).map((c) => (
          <button
            key={c}
            type="button"
            onClick={() => setColor(c)}
            className={`aspect-square rounded-lg overflow-hidden border ${
              color === c ? "border-emerald-400" : "border-white/10"
            }`}
            title={c}
          >
            <img src={images[c]} className="size-full object-cover" loading="lazy" />
          </button>
        ))}
      </div>
    </div>
  );
}
