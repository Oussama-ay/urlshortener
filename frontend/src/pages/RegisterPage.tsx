import { useState } from "react";
import type { FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";

function RegisterPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (loading) return;
    setLoading(true);
    setError("");

    try {
      const apiUrl = import.meta.env.VITE_API_URL;
      if (!apiUrl) throw new Error("Registration is unavailable. Please try again later.");

      const response = await fetch(`${apiUrl.replace(/\/$/, "")}/api/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        const data: unknown = await response.json().catch(() => null);
        const message = data && typeof data === "object" && "message" in data
          && typeof data.message === "string" && data.message
          ? data.message : "Registration failed. Please try again.";
        throw new Error(message);
      }

      navigate("/login");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <Navbar />
      <main className="auth-page">
        <div className="auth-card">
          <h1>Create account</h1>
          <form onSubmit={handleSubmit} aria-busy={loading}>
            <label htmlFor="register-email">Email</label>
            <input
              id="register-email"
              name="email"
              type="email"
              autoComplete="email"
              placeholder="Email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
              disabled={loading}
            />
            <label htmlFor="register-password">Password</label>
            <input
              id="register-password"
              name="password"
              type="password"
              autoComplete="new-password"
              placeholder="Password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
              disabled={loading}
            />
            {error && <p className="error-message" role="alert">{error}</p>}
            <button type="submit" disabled={loading}>
              {loading ? "Creating account..." : "Sign Up"}
            </button>
          </form>
          <p>Already have an account? <Link to="/login">Login</Link></p>
        </div>
      </main>
    </>
  );
}

export default RegisterPage;
