export default function Empty({
  title = "No results",
  hint,
}: { title?: string; hint?: string }) {
  return (
    <div className="rounded-2xl border border-white/10 bg-[#121317] p-8 text-center">
      <svg width="64" height="64" viewBox="0 0 24 24" className="mx-auto opacity-60">
        <path fill="currentColor" d="M12 3c-3.9 0-7 3.1-7 7c0 5.3 7 11 7 11s7-5.7 7-11c0-3.9-3.1-7-7-7m0 9.5a2.5 2.5 0 1 1 0-5a2.5 2.5 0 0 1 0 5" />
      </svg>
      <div className="mt-3 text-base font-medium">{title}</div>
      {hint && <div className="mt-1 text-xs text-neutral-400">{hint}</div>}
    </div>
  );
}
