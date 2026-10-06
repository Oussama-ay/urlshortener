import { useState } from "react";

type UrlResponse = {
  shortCode: string;
  shortUrl: string;
};

type ApiError = {
  message?: string;
};

function ShortenForm() {
  const [url, setUrl] = useState("");
  const [result, setResult] = useState<UrlResponse | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [copied, setCopied] = useState(false);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setLoading(true);
    setError("");
    setResult(null);
    setCopied(false);

    try {
      const response = await fetch(
        `${import.meta.env.VITE_API_URL}/api/urls`,
        {
          method: "POST",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ url }),
        }
      );

      if (!response.ok) {
        const errorData: ApiError = await response.json().catch(() => ({}));

        throw new Error(errorData.message || "Failed to shorten URL");
      }

      const data: UrlResponse = await response.json();

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
          placeholder="Shorten a link here..."
          value={url}
          onChange={(event) => setUrl(event.target.value)}
        />

        <button type="submit" disabled={loading}>
          {loading ? "Shortening..." : "Shorten It!"}
        </button>
      </form>

      {error && <p className="error-message">{error}</p>}

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