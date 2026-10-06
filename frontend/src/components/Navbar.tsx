import { useEffect, useState } from "react";
import LogoutButton from "./LogoutButton";
import { Link } from "react-router-dom";

function Navbar() {
  const [authenticated, setAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

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

  return (
    <nav>
      <Link to="/" aria-label="Shortly home">
        <img src="/images/logo.svg" alt="Shortly" />
      </Link>

      <div className="nav-auth">
        {!loading && (authenticated ? (
          <>
            <Link className="nav-action" to="/my-links">My Links</Link>
            <LogoutButton onLogout={() => setAuthenticated(false)} />
          </>
        ) : (
          <>
            <Link className="nav-action" to="/login">Login</Link>
            <Link className="nav-action nav-signup" to="/register">Sign Up</Link>
          </>
        ))}
      </div>
    </nav>
  );
}

export default Navbar;
