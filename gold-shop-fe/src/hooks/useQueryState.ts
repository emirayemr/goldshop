import { useEffect, useMemo, useState } from "react";

export function useQueryState<T extends Record<string, any>>(
  initial: T,
  encode: (v: T) => URLSearchParams,
  decode: (p: URLSearchParams) => Partial<T>
) {
  const params = useMemo(() => new URLSearchParams(window.location.search), []);
  const fromUrl = decode(params);
  const [state, setState] = useState<T>({ ...initial, ...fromUrl });

  // write to URL when state changes
  useEffect(() => {
    const sp = encode(state);
    const url = `${window.location.pathname}?${sp.toString()}`;
    window.history.replaceState(null, "", url);
  }, [state, encode]);

  return [state, setState] as const;
}
