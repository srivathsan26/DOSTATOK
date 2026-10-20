# Feature Audit

Audit state after the authentication and clean-start implementation pass.

| Area | State | Evidence or remaining work |
|---|---|---|
| Registration and login | Implemented | Java API, password hashing, validation, generic login errors; covered by `SecurityTestSuite` |
| Sessions and logout | Implemented locally | Random server-side session IDs, 8-hour expiry, HttpOnly/SameSite cookie, logout; sessions are memory-only |
| User ownership | Implemented at API boundary | Resource ownership checks for account, income, expense, investment, asset, loan, and goal routes; related account IDs and transfers checked |
| Database migration | Implemented | `PRAGMA user_version`, transactional migration; tested on a copy before active DB migration |
| Fictional demo data | Removed from active database | Tables remain empty; normal startup does not seed; `database/seed.sql` is legacy fixture only |
| OOP architecture navigation | Removed | OOP remains in Java demo and standalone documentation |
| Dark/light theme | Implemented | CSS variables; choice stored in browser local storage |
| Accounts, cash flow, investments, assets, loans, goals | Existing CRUD retained | Thorough field-by-field financial and workflow audit remains to be completed |
| Financial atomicity and accounting | Partially reviewed | Existing service/repository workflows retained; full rollback and net-worth audit remains |
| Frontend form validation and empty states | Partially reviewed | Existing pages retained; all visible controls and responsive behavior need manual browser review |
| Automated backend tests | Partial | 41 isolated checks cover auth, migration, session lifecycle, all owned financial record routes, and transfer ownership; legacy seeded-data suite needs redesign |
| Production security | Not claimed | Local HTTP, in-memory sessions, no HTTPS deployment or external penetration testing |
