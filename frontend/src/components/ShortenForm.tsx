import { useState } from "react";

type UrlResponse = {
  shortCode: string;
  shortUrl: string;
};

function ShortenForm() {
  const [url, setUrl] = useState("");
  const [result, setResult] = useState<UrlResponse | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setLoading(true);
    setError("");
    setResult(null);

    try {
      const response = await fetch(
        `${import.meta.env.VITE_API_URL}/api/urls`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            url: url,
          }),
        }
      );

      if (!response.ok) {
        throw new Error("Failed to shorten URL");
      }

      const data: UrlResponse = await response.json();

      setResult(data);
    } catch (err) {
      setError("Something went wrong");
    } finally {
      setLoading(false);
    }
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

      {error && <p>{error}</p>}

      {result && (
        <div>
          <p>{url}</p>
          <a href={result.shortUrl}>
            {result.shortUrl}
          </a>
        </div>
      )}
    </section>
  );
}

export default ShortenForm;