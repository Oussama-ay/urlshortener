import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { getPendingUrl } from "../services/authNavigation";

type UrlResponse = {
  shortCode: string;
  shortUrl: string;
};

type ApiError = {
  message?: string;
};

function ShortenForm() {
  const location = useLocation();
  const navigate = useNavigate();
  const [url, setUrl] = useState(() => getPendingUrl(location.state) ?? "");
  const [result, setResult] = useState<UrlResponse | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [copied, setCopied] = useState(false);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (loading) return;

    setLoading(true);
    setError("");
    setResult(null);
    setCopied(false);

    try {
      const apiUrl = import.meta.env.VITE_API_URL;
      if (!apiUrl) throw new Error("URL creation is unavailable. Please try again later.");
      const response = await fetch(
        `${apiUrl.replace(/\/$/, "")}/api/urls`,
        {
          method: "POST",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ url }),
        }
      );

      if (response.status === 401) {
        navigate("/login", { state: { pendingUrl: url } });
        return;
      }

      if (!response.ok) {
        const errorData: ApiError = await response.json().catch(() => ({}));

        throw new Error(errorData?.message || "Failed to shorten URL");
      }

      const data: UrlResponse = await response.json();

      navigate(location.pathname, { replace: true, state: null });
      setResult(data);
      setCopied(false);
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError("Something went wrong");
      }
    } finally {
      setLoading(false);
    }
  }

  async function handleCopy() {
    if (!result) return;

    await navigator.clipboard.writeText(result.shortUrl);

    setCopied(true);

    setTimeout(() => {
      setCopied(false);
    }, 2000);
  }

  return (
    <section className="shorten-section">
      <form className="shorten-form" onSubmit={handleSubmit}>
        <input
          type="url"
          aria-label="URL to shorten"
          required
          disabled={loading}
          placeholder="Shorten a link here..."
          value={url}
          onChange={(event) => setUrl(event.target.value)}
        />

        <button type="submit" disabled={loading}>
          {loading ? "Shortening..." : "Shorten It!"}
        </button>
      </form>

      {error && <p className="error-message" role="alert">{error}</p>}

      {result && (
        <div className="url-result">
          <p className="original-url">{url}</p>

          <div className="short-url-section">
            <a
              href={result.shortUrl}
              target="_blank"
              rel="noreferrer"
            >
              {result.shortUrl}
            </a>

            <button
              type="button"
              onClick={handleCopy}
            >
              {copied ? "Copied!" : "Copy"}
            </button>
          </div>
        </div>
      )}
    </section>
  );
}

export default ShortenForm;