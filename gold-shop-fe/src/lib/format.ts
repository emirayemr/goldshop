export const fmtUsd = (n: number) =>
  new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(n);

export const parseNum = (s: string | undefined) =>
  s === undefined || s === "" ? undefined : Number(s);
