# Testing and Verification Record

This document records commands actually executed during the implementation pass. Earlier test claims were not treated as evidence.

## Observed results

| Check | Command / method | Result |
|---|---|---|
| Java compilation | `javac -encoding UTF-8 -cp ...` from `backend/` | Passed after the authentication and migration changes |
| Frontend production build | `npm.cmd run build` from `frontend/` | Passed; Vite 8.3.4 built 1,921 modules |
| SQLite pre-change backup | JDBC `PRAGMA integrity_check` on active DB and backup | Both reported `ok`; initial counts were 1 user, 3 accounts, 14 transactions, 6 income rows, 7 expenses, 4 investments, 2 assets, 1 loan, 1 repayment, and 3 goals |
| Migration rehearsal | `SecurityTestSuite` on a disposable copy of the verified backup | Passed migration checks; original demo records were removed only in the test copy |
| Security and ownership | `SecurityTestSuite` on disposable DB | 41 checks passed: registration, duplicate/invalid registration, successful and failed login, sessions/logout, unauthenticated access, cross-user financial record isolation, cross-owner transfer rejection, password hash storage, and persistence |
| Active database migration | `java ... com.dostatok.main.DbCheck` after rehearsal | Passed; all schema tables remained present |
| Active database integrity | JDBC `PRAGMA integrity_check` after migration | `ok`; user and financial table row counts are all zero |
| Full backend startup smoke check | `App` on isolated port 18081 and temporary DB; `GET /api/health` and `GET /api/auth/me` | Server started; health returned 200 and unauthenticated profile returned 401 |
| Launcher conflict check | `cmd /c run-app.bat` while port 8080 was occupied by an older API | Correctly reported the incompatible API and did not launch Vite |

Run the reusable isolated suite from `backend/` with `test-security.bat`. It copies the pre-implementation DB backup, runs the suite, and removes its randomly named temporary copy.

## Not yet verified

- The legacy `BackendTestSuite` assumes seeded data and has not been run against the personal database.
- Full account, cash-flow, investment, asset, loan, repayment, and goal CRUD scenarios need isolated workflow tests.
- Every monetary boundary, transaction rollback path, net-worth formula, and repayment accounting rule has not been independently audited.
- Browser workflows, responsive layout, visual theme behavior, accessibility, and console errors still need manual browser verification.
- Expiry timing has not been tested with an elapsed 8-hour clock; the session code checks expiration on each lookup.

Do not describe these unverified areas as passing. The active database backup is retained at `database/dostatok-pre-implementation-20261014-223314.db` and is excluded from Git with other `.db` files.
