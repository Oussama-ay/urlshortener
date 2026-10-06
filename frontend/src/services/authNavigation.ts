export function getPendingUrl(state: unknown): string | null {
  if (state && typeof state === "object" && "pendingUrl" in state
      && typeof state.pendingUrl === "string") {
    return state.pendingUrl;
  }
  return null;
}
