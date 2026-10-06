import { useState } from "react";
import { useNavigate } from "react-router-dom";

function Hero() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  async function handleGetStarted() {
    if (loading) return;
    setLoading(true);
    setError("");
    try {
      const apiUrl = import.meta.env.VITE_API_URL;
      if (!apiUrl) throw new Error("Please try again later.");
      const response = await fetch(`${apiUrl.replace(/\/$/, "")}/api/auth/me`, {
        credentials: "include",
        cache: "no-store",
      });
      if (response.ok) navigate("/my-links");
      else if (response.status === 401) navigate("/register");
      else throw new Error("Unable to check your account. Please try again.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="hero">
      <div className="hero-content">
        <h1>More than just shorter links</h1>

        <p>
          Build your brand's recognition and get detailed insights on how your
          links are performing.
        </p>

        <button type="button" onClick={handleGetStarted} disabled={loading}>
          {loading ? "Loading..." : "Get Started"}
        </button>
        {error && <p className="error-message" role="alert">{error}</p>}
      </div>

      <div className="hero-image">
        <img
          src="/images/illustration-working.svg"
          alt="Person working"
        />
      </div>
    </section>
  );
}

export default Hero;