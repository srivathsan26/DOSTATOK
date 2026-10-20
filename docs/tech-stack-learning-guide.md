# DOSTATOK Technology Stack Guide

## What each part does

- **React and JavaScript** render screens, hold temporary form state, and react to user actions (`frontend/src/App.jsx`).
- **Vite** serves the development app and bundles the production build (`frontend/vite.config.js`).
- **Java** owns API behavior and financial rules. `javac` compiles source into class files; `java` runs those files.
- **HttpServer** accepts local HTTP requests (`backend/src/com/dostatok/main/App.java`). JSON carries request and response data.
- **Controllers** map paths and HTTP methods (`ApiController`). **Services** validate operations (`AccountService`, `FinanceService`). **Models** represent the domain. **Repositories** implement persistence.
- **JDBC** is Java's interface to SQL. SQLite stores relational data in `database/dostatok.db`.
- **Primary keys** identify rows. **Foreign keys** link records and protect referential integrity. Indexes make user-scoped queries faster.
- **Authentication** hashes passwords with JDK `PBKDF2WithHmacSHA256`; an HttpOnly cookie carries a random session identifier. The session-to-user mapping remains in backend memory.
- **Git** records source changes. `.gitignore` excludes local SQLite database files and build output.

## One request from form to database

For an account deposit:

1. React validates required form fields.
2. `frontend/src/services/api.js` sends JSON to `POST /api/accounts/{id}/deposit` with the session cookie.
3. `ApiController` resolves the cookie, identifies the signed-in user, checks the account belongs to that user, and parses the request.
4. `AccountService` validates the amount and balance rules.
5. A repository updates the account and records the ledger transaction in SQLite.
6. Java returns a JSON response; React refreshes the affected data and shows a result.

If validation fails, the backend returns an error status. If the session is absent or expired, financial routes return 401. If the record belongs to someone else, the API returns 404. Database failures are rolled back where the operation uses a transaction and produce a generic server error response.

## Local security limits

This is a local college project, not a production security service. Sessions are in memory and end when the backend restarts. The local server uses HTTP, so cookies do not receive HTTPS transport protection. If HTTPS terminates at a trusted proxy, set `-DDOSTATOK_SECURE_COOKIES=true` for the backend. Use it only on a trusted machine and do not expose the API to the public internet.

## Dependency classpath

The backend uses manually managed JARs in `backend/lib`. Compile and launch commands are listed in the root README and `backend/run.bat`; no Maven or Gradle build is used.
