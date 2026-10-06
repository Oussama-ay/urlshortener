import { useEffect, useState } from "react";
import { Navigate, Outlet } from "react-router-dom";

function ProtectedRoute() {
  const [loading, setLoading] = useState(true);
  const [authenticated, setAuthenticated] = useState(false);

  useEffect(() => {
    const controller = new AbortController();

    async function checkAuth() {
      try {
        const apiUrl = import.meta.env.VITE_API_URL;
        if (!apiUrl) throw new Error("API URL is not configured");
        const response = await fetch(`${apiUrl.replace(/\/$/, "")}/api/auth/me`, {
          credentials: "include",
          cache: "no-store",
          signal: controller.signal,
        });
        if (!controller.signal.aborted) setAuthenticated(response.ok);
      } catch {
        if (!controller.signal.aborted) setAuthenticated(false);
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    }

    void checkAuth();
    return () => controller.abort();
  }, []);

  if (loading) return <p className="page-content" role="status">Loading...</p>;
  if (!authenticated) return <Navigate to="/login" replace />;
  return <Outlet />;
}

export default ProtectedRoute;
