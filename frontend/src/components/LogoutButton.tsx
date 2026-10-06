import { useState } from "react";
import { useNavigate } from "react-router-dom";

function LogoutButton() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  async function handleLogout() {
    if (loading) return;
    setLoading(true);
    setError("");
    try {
      const apiUrl = import.meta.env.VITE_API_URL;
      if (!apiUrl) throw new Error("Logout is unavailable. Please try again later.");
      const response = await fetch(`${apiUrl.replace(/\/$/, "")}/api/auth/logout`, {
        method: "POST",
        credentials: "include",
      });
      if (!response.ok) {
        const data: unknown = await response.json().catch(() => null);
        const message = data && typeof data === "object" && "message" in data
          && typeof data.message === "string" && data.message
          ? data.message : "Failed to log out. Please try again.";
        throw new Error(message);
      }
      navigate("/login", { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to log out. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <button className="logout-button" type="button" onClick={handleLogout} disabled={loading}>
        {loading ? "Logging out..." : "Logout"}
      </button>
      {error && <p className="error-message" role="alert">{error}</p>}
    </div>
  );
}

export default LogoutButton;
