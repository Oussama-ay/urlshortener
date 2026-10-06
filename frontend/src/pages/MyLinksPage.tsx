import { useEffect, useState } from "react";
import Navbar from "../components/Navbar";

type UrlItem = {
  originalUrl: string;
  shortCode: string;
  shortUrl: string;
  createdAt: string;
  expiresAt: string | null;
  clickCount: number;
  lastAccessedAt: string | null;
};

type UrlPage = {
  content: UrlItem[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};

function MyLinksPage() {
  const [data, setData] = useState<UrlPage | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const controller = new AbortController();

    async function fetchLinks() {
      try {
        const apiUrl = import.meta.env.VITE_API_URL;
        if (!apiUrl) throw new Error("Links are unavailable. Please try again later.");

        const response = await fetch(
          `${apiUrl.replace(/\/$/, "")}/api/urls?page=0&size=10&sort=createdAt,desc`,
          { credentials: "include", signal: controller.signal },
        );

        if (!response.ok) {
          const errorData: unknown = await response.json().catch(() => null);
          const message = errorData && typeof errorData === "object" && "message" in errorData
            && typeof errorData.message === "string" && errorData.message
            ? errorData.message : "Failed to load links";
          throw new Error(message);
        }

        const result: UrlPage = await response.json();
        if (!controller.signal.aborted) setData(result);
      } catch (err) {
        if (!controller.signal.aborted) {
          setError(err instanceof Error ? err.message : "Something went wrong");
        }
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    }

    void fetchLinks();
    return () => controller.abort();
  }, []);

  return (
    <>
      <Navbar />
      <main className="links-page">
        <h1>My Links</h1>
        {loading ? (
          <p role="status">Loading links...</p>
        ) : error ? (
          <p className="error-message" role="alert">{error}</p>
        ) : data?.content.length === 0 ? (
          <p>You haven't created any links yet.</p>
        ) : (
          <div className="links-list">
            {data?.content.map((url) => (
              <article key={url.shortCode} className="link-card">
                <p>{url.originalUrl}</p>
                <a href={url.shortUrl} target="_blank" rel="noreferrer">
                  {url.shortUrl}
                </a>
                <p>Clicks: {url.clickCount}</p>
              </article>
            ))}
          </div>
        )}
      </main>
    </>
  );
}

export default MyLinksPage;
