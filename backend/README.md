# DOSTATOK Backend

Plain Java 27 HTTP API using JDK `com.sun.net.httpserver.HttpServer`, SQLite JDBC 3.53.4.0, and Gson 2.11.0. Compile with `javac`; run with `java`. Maven, Gradle, and Spring Boot are not used.

## Start the backend

From this directory, run:

```cmd
run.bat
```

The launcher compiles the source into `out/` and starts the API at `http://localhost:8080`. The default SQLite path is `../database/dostatok.db`; set `DOSTATOK_DB_PATH` to override it. Startup applies versioned schema migrations and never seeds demo data.

## Tests and OOP demonstration

Run isolated authentication, migration, and ownership tests from this directory:

```cmd
test-security.bat
```

The script uses a disposable copy of the verified pre-migration backup and removes the copy afterward. Do not run `BackendTestSuite` against personal data; it expects seeded records and writes to its configured DB.

From the project root, run the standalone Java OOP demonstration:

```cmd
java -cp "backend\out;backend\lib\sqlite-jdbc-3.53.4.0.jar;backend\lib\gson-2.11.0.jar" com.dostatok.main.OopDemo
```

## Package responsibilities

- `model`: domain state and behavior.
- `service`: validation, authentication, sessions, and financial workflows.
- `dao`: JDBC repositories and persistence.
- `controller`: HTTP routing, JSON, origin checks, and authorization.
- `database`: path resolution and versioned migrations.
- `exception`: application and domain failures.
- `util`: JSON and currency helpers.

The local app uses HTTP and in-memory sessions. See the root README for the security limits and project learning guides.
