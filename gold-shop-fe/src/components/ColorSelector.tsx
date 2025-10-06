import { clsx } from "clsx";

type Color = "yellow" | "white" | "rose";

export default function ColorSelector({
  value,
  onChange,
}: {
  value: Color;
  onChange: (c: Color) => void;
}) {
  const dot = (c: Color) =>
    clsx(
      "size-5 rounded-full border ring-offset-2 focus:outline-none focus:ring-2",
      c === "yellow" && "bg-yellow-300 border-yellow-300",
      c === "white" && "bg-zinc-200 border-zinc-200",
      c === "rose" && "bg-rose-300 border-rose-300",
      value === c ? "ring-emerald-400" : "ring-transparent"
    );

  return (
    <div className="flex items-center gap-2" role="radiogroup" aria-label="Metal color">
      {(["yellow", "white", "rose"] as const).map((c) => (
        <button
          key={c}
          type="button"
          aria-checked={value === c}
          role="radio"
          className={dot(c)}
          title={c}
          onClick={() => onChange(c)}
        />
      ))}
    </div>
  );
}
