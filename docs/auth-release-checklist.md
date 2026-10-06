# Authentication release checks

Validation date: 2026-10-06. Feature branch: `feat/user-registration`.
The branch was updated by merging `origin/main`, preserving its published history.

## Completed

| Check | Evidence |
| --- | --- |
| Backend clean build and tests | 40 tests passed, then repeated successfully with the separate `url_shortener_test` database. |
| Frontend dependencies and checks | `npm ci`, production build, and ESLint passed; npm reported zero vulnerabilities. |
| Browser registration and login | Fresh accounts registered and logged in through the actual React forms against the packaged backend. |
| Cookie attributes | Chromium confirmed `HttpOnly`, `Secure`, `SameSite=None`, and path `/`; the JWT was absent from `document.cookie`. |
| Session persistence | Reload preserved login and allowed authenticated requests. |
| Authentication guard and logout | Logged-out My Links redirected to login; logout removed the cookie, `/me` returned 401, and revisiting My Links redirected to login. |
| URL ownership | User A created a URL; A could list/read it, B received 404 for its details and saw only B's URLs. |
| Public redirects and expiration | Anonymous redirect returned 302 with the correct destination; expired URLs returned 410. |
| Rate limiting | Using a fresh client IP, requests 1–20 returned 201 and request 21 returned 429. |
| Redis caching | Cached destination matched the URL; positive TTL was below the 24-hour limit; rate-limit key also had a TTL. |
| PostgreSQL migrations and ownership | All five migrations succeeded on a fresh release database; SQL confirmed the URL's owner. |
| CORS and CSRF | The explicit test frontend origin received credentialed CORS permission; a foreign-origin write was rejected. |
| Compose configuration | Standalone Compose parsed the file and verified that `backend/.env` supplies JWT configuration. |
| Secrets and repository cleanup | Local secret values were absent from tracked files; no real `.env`, `target`, `node_modules`, editor backup, or debugging print files were tracked. |

Browser tests used the built frontend at `http://localhost:5174` and packaged
backend at `http://localhost:8090`, with secure `SameSite=None` cookies.
These are local browser checks, not verification of the Vercel/Render domains.

## Remaining before merge

- Run `docker compose up --build` on a machine with Docker. This environment has
  no Docker executable or daemon, so container image builds and container
  networking have not been verified.
- Compose starts the database, Redis, and backend; the frontend runs separately.
  Point its `VITE_API_URL` at `http://localhost:8080` for the container test.
- Verify the deployed preview/staging frontend and backend use the correct
  `APP_FRONTEND_URL`, `APP_BASE_URL`, private `JWT_SECRET`, and HTTPS cookie flags.
  Test credentialed requests on the actual domains, including browser
  third-party-cookie settings.
- Review the PR and check any hosting/CI results before merging.

## After merge

Update `main`, repeat the Docker and browser smoke tests, then deploy and verify
production cookie/CORS behavior and logs. Create a release tag only after those
checks pass. No merge, release tag, or production deployment was performed by
this validation run.
