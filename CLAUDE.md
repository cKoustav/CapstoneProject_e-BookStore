# eBookStore

Spring Boot 3.3.4 (Java 17) e-commerce bookstore. Auth (SHA-256 + bearer tokens), catalog, cart, checkout with a mock payment gateway, and 48h order cancellation — all persisted as JSON files, no database.

## Architecture

- `com.ebookstore.model` — `Book`, `User`, `Order`, `OrderItem`, `Address`, `PaymentDetails`, `OrderStatus`
- `com.ebookstore.repository` — `AbstractFileRepository` (shared file-based CRUD) with `BookRepository`, `UserRepository`, `OrderRepository` on top
- `com.ebookstore.service` — `BookService`, `AuthService`, `OrderService`, `MockPaymentGatewayService`
- `com.ebookstore.controller` — `BookController`, `AuthController`, `OrderController`
- Data files: `data/books.json`, `data/orders.json`, (users similarly) — configured via `app.storage.directory=data` in `application.properties`

**Frontend is plain static HTML/CSS/JS** served from `src/main/resources/static/` (no Thymeleaf/JSP/SPA framework):
- `index.html` — the only HTML file; a one-page app that toggles `.view-section` visibility to fake routing
- `css/styles.css` — the only stylesheet; colors are CSS custom properties in `:root` (and a `[data-theme="dark"]` override block for dark mode — see below)
- `js/app.js` — the only script; holds a global `state` object and all client logic

## Key operational gotcha: stopping the app

`mvn spring-boot:run` **forks a child JVM**. Stopping the wrapping bash/task (`TaskStop`, Ctrl+C on the shell) does **not** kill that child — it keeps listening on port 8080 and the next `spring-boot:run` fails with "Port 8080 was already in use." To actually stop it:

```bash
netstat -ano | grep ':8080' | grep LISTENING   # find the orphaned PID
powershell -NoProfile -Command "Stop-Process -Id <PID> -Force"
```

Always check for this before restarting the app during a session.

- No `mvnw`/`mvnw.cmd` wrapper exists — use `mvn` directly (found at `C:\Program Files\apache-maven-3.9.16\bin\mvn`).
- App runs on `http://localhost:8080`, port set in `application.properties`.

## File-based storage caveat

Repositories load JSON data into memory at startup. Editing `data/*.json` directly while the app is running has no effect until restart — the in-memory copy is what's served (and what gets written back on save). `BookService.seedInitialBooks()` only seeds `data/books.json` when the repository is empty (first run ever), so subsequent edits to book data must go in `data/books.json` directly (and ideally also in the seed method in `BookService.java` for consistency/fresh checkouts).

## Light/Dark theme (added 2026-09-25)

- Toggle button `#theme-toggle-btn` (🌙/☀️) is the last child of `.nav-links` in the navbar — top-right of the page.
- Theme is stored as `data-theme="light"|"dark"` on `<html>`, persisted in `localStorage` under `ebookstore_theme`.
- An inline script in `index.html`'s `<head>` (right after the `styles.css` link) applies the saved/preferred theme before first paint to avoid a flash of the wrong theme.
- `app.js`: `initTheme()` (called from `DOMContentLoaded`) and `toggleTheme()` own the logic; `updateThemeToggleIcon()` swaps the button glyph.
- `styles.css`: dark palette lives in a `[data-theme="dark"] { ... }` block right after `:root`, overriding the same variable names. A few previously-hardcoded colors (order-status badges, cancellation banner, modal close button, order footer, outline-danger hover, test-fail box) got dedicated dark overrides or were switched to use the variables so they adapt automatically.
- Hero banner, footer, and toasts were already dark-themed by design and were left untouched — they work on both themes.

## Verifying frontend changes

No project skill for launching the app existed as of this note — verified manually with `mvn spring-boot:run` + a throwaway Playwright script (installed on demand via `npm install playwright` + `npx playwright install chromium`, no `chromium-cli` available in this environment). Consider running `/run-skill-generator` if this becomes a recurring need.
