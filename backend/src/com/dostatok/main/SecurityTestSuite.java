package com.dostatok.main;

import com.dostatok.controller.ApiController;
import com.dostatok.dao.AccountRepositoryImpl;
import com.dostatok.dao.FinanceRepositoryImpl;
import com.dostatok.dao.TransactionRepositoryImpl;
import com.dostatok.database.DatabaseConfig;
import com.dostatok.service.AccountService;
import com.dostatok.service.AuthService;
import com.dostatok.service.FinanceService;
import com.dostatok.service.SessionStore;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.Map;

/** Isolated migration, authentication, and ownership tests. Use a copied test DB via DOSTATOK_DB_PATH. */
public final class SecurityTestSuite {
    private static int passed;
    private SecurityTestSuite() {}

    public static void main(String[] args) throws Exception {
        if (System.getProperty("DOSTATOK_DB_PATH") == null && System.getenv("DOSTATOK_DB_PATH") == null) {
            throw new IllegalStateException("Set DOSTATOK_DB_PATH to an isolated test database copy.");
        }
        DatabaseConfig.initializeDatabase();
        try (Connection c = DatabaseConfig.getConnection(); Statement s = c.createStatement()) {
            try (ResultSet r = s.executeQuery("SELECT count(*) FROM users WHERE email='aditya.sharma@dostatok.in'")) {
                r.next(); check(r.getInt(1) == 0, "migration removes retired demo profile");
            }
            try (ResultSet r = s.executeQuery("PRAGMA user_version")) { r.next(); check(r.getInt(1) == 1, "migration records schema version 1"); }
        }

        var accountRepo = new AccountRepositoryImpl();
        var txRepo = new TransactionRepositoryImpl();
        var financeRepo = new FinanceRepositoryImpl();
        var accountService = new AccountService(accountRepo, txRepo);
        var financeService = new FinanceService(financeRepo, accountRepo, txRepo, accountService);
        ApiController controller = new ApiController(accountService, financeService);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api", controller);
        server.start();
        try {
            String base = "http://127.0.0.1:" + server.getAddress().getPort() + "/api";
            HttpClient client = HttpClient.newHttpClient();
            Reply unauth = send(client, base, "GET", "/accounts", "", null);
            check(unauth.status == 401, "unauthenticated financial API access is denied");

            Reply a = send(client, base, "POST", "/auth/register", "{\"name\":\"User A\",\"email\":\"a@example.test\",\"password\":\"correct horse battery\"}", null);
            check(a.status == 201, "registration creates a user and session");
            String cookieA = a.cookie;
            int accountA = createAccount(client, base, cookieA, "USER-A-1");
            int accountA2 = createAccount(client, base, cookieA, "USER-A-2");
            Reply duplicate = send(client, base, "POST", "/auth/register", "{\"name\":\"User A\",\"email\":\"A@EXAMPLE.TEST\",\"password\":\"correct horse battery\"}", null);
            check(duplicate.status == 400, "email uniqueness is case-insensitive");
            Reply invalidLogin = send(client, base, "POST", "/auth/login", "{\"email\":\"a@example.test\",\"password\":\"wrong password\"}", null);
            check(invalidLogin.status == 401 && invalidLogin.body.contains("Email or password is incorrect"), "invalid login uses a generic response");
            Reply badRegistration = send(client, base, "POST", "/auth/register", "{\"name\":\"\",\"email\":\"bad\",\"password\":\"short\"}", null);
            check(badRegistration.status == 400, "invalid registration is rejected");

            Reply b = send(client, base, "POST", "/auth/register", "{\"name\":\"User B\",\"email\":\"b@example.test\",\"password\":\"another correct horse\"}", null);
            check(b.status == 201, "second user registration creates an independent session");
            int userB = JsonParser.parseString(b.body).getAsJsonObject().getAsJsonObject("data").get("id").getAsInt();
            int accountB = createAccount(client, base, b.cookie, "USER-B-1");
            check(send(client, base, "GET", "/accounts/" + accountB, "", cookieA).status == 404, "user A cannot read user B account");
            check(send(client, base, "PUT", "/accounts/" + accountB, "{}", cookieA).status == 404, "user A cannot modify user B account");
            check(send(client, base, "DELETE", "/accounts/" + accountB, "", cookieA).status == 404, "user A cannot delete user B account");
            check(send(client, base, "POST", "/accounts/transfer", "{\"fromAccountId\":" + accountA + ",\"toAccountId\":" + accountB + ",\"amount\":1}", cookieA).status == 404,
                "transfers cannot target another user's account");
            Map<String, Integer> fixtures = createFinancialFixtures(userB, accountB);
            for (String resource : new String[]{"income", "expenses", "investments", "assets", "loans", "goals"}) {
                int id = fixtures.get(resource);
                check(send(client, base, "GET", "/" + resource + "/" + id, "", cookieA).status == 404,
                    "user A cannot read user B " + resource);
                check(send(client, base, "PUT", "/" + resource + "/" + id, "{}", cookieA).status == 404,
                    "user A cannot modify user B " + resource);
                check(send(client, base, "DELETE", "/" + resource + "/" + id, "", cookieA).status == 404,
                    "user A cannot delete user B " + resource);
            }
            check(send(client, base, "GET", "/transactions", "", cookieA).body.contains("fixture")==false,
                "user A transaction list does not include user B ledger data");
            check(send(client, base, "GET", "/loans/" + fixtures.get("loans") + "/repayments", "", cookieA).status == 404,
                "user A cannot read user B loan repayments");
            check(send(client, base, "GET", "/accounts/" + accountB + "/transactions", "", cookieA).status == 404,
                "user A cannot read user B account transactions");
            check(send(client, base, "POST", "/accounts/" + accountA + "/deposit", "{\"amount\":1}", cookieA).status != 401,
                "valid session reaches an owned account operation");
            Reply logout = send(client, base, "POST", "/auth/logout", "{}", cookieA);
            check(logout.status == 200 && send(client, base, "GET", "/accounts", "", cookieA).status == 401, "logout invalidates the session");

            AuthService auth = new AuthService();
            Reply successfulLogin = send(client, base, "POST", "/auth/login", "{\"email\":\"b@example.test\",\"password\":\"another correct horse\"}", null);
            check(successfulLogin.status == 200, "valid credentials sign in through the API");
            check(send(client, base, "GET", "/auth/me", "", successfulLogin.cookie).status == 200,
                "profile endpoint resolves the authenticated session");
            check(auth.authenticate("b@example.test", "another correct horse".toCharArray()) != null, "user credentials persist in SQLite");
            try (Connection c = DatabaseConfig.getConnection(); Statement s = c.createStatement();
                 ResultSet r = s.executeQuery("SELECT password_hash FROM users WHERE email='b@example.test'")) {
                check(r.next() && !r.getString(1).contains("another correct horse") && r.getString(1).startsWith("pbkdf2-sha256$"), "only a password hash is persisted");
            }
            SessionStore sessions = new SessionStore();
            String id = sessions.create(42);
            check(sessions.resolve(id) == 42, "new session resolves to its server-side user");
            sessions.invalidate(id);
            check(sessions.resolve(id) == null, "invalidated session no longer resolves");
            System.out.println("SecurityTestSuite: " + passed + " checks passed.");
        } finally { server.stop(0); }
    }

    private static int createAccount(HttpClient client, String base, String cookie, String number) throws Exception {
        String body = "{\"accountNumber\":\"" + number + "\",\"accountName\":\"Test Account\",\"accountType\":\"SAVINGS\",\"bankName\":\"Local Test Bank\",\"initialBalance\":100,\"minimumBalance\":0,\"overdraftLimit\":0,\"interestRate\":0}";
        Reply reply = send(client, base, "POST", "/accounts", body, cookie);
        if (reply.status != 201) throw new AssertionError("Account setup failed: HTTP " + reply.status + " " + reply.body);
        return JsonParser.parseString(reply.body).getAsJsonObject().getAsJsonObject("data").get("id").getAsInt();
    }

    private static Map<String, Integer> createFinancialFixtures(int userId, int accountId) throws Exception {
        java.util.Map<String, Integer> ids = new java.util.LinkedHashMap<>();
        try (Connection c = DatabaseConfig.getConnection()) {
            ids.put("income", insert(c, "INSERT INTO income(user_id,account_id,title,amount,category,source,income_date) VALUES(?,?,?,10,'Other','Test','2026-01-01')", userId, accountId, "fixture-income"));
            ids.put("expenses", insert(c, "INSERT INTO expenses(user_id,account_id,title,amount,category,payment_method,expense_date) VALUES(?,?,?,10,'Other','Cash','2026-01-01')", userId, accountId, "fixture-expense"));
            ids.put("investments", insert(c, "INSERT INTO investments(user_id,name,investment_type,invested_amount,current_value,purchase_date) VALUES(?,?,'Test',10,10,'2026-01-01')", userId, "fixture-investment"));
            ids.put("assets", insert(c, "INSERT INTO assets(user_id,name,asset_category,purchase_value,current_value,acquisition_date) VALUES(?,?,'MOVABLE',10,10,'2026-01-01')", userId, "fixture-asset"));
            ids.put("loans", insert(c, "INSERT INTO loans(user_id,loan_name,lender,principal_amount,interest_rate,tenure_months,start_date,outstanding_balance,monthly_emi) VALUES(?,?,'Test',10,0,1,'2026-01-01',10,10)", userId, "fixture-loan"));
            ids.put("goals", insert(c, "INSERT INTO financial_goals(user_id,title,target_amount,current_amount,target_date,category) VALUES(?,?,10,0,'2027-01-01','Test')", userId, "fixture-goal"));
            try (PreparedStatement p = c.prepareStatement("INSERT INTO transactions(user_id,account_id,transaction_type,amount,balance_after,category,description) VALUES(?,?,'DEPOSIT',1,101,'Test','fixture-ledger')")) {
                p.setInt(1, userId); p.setInt(2, accountId); p.executeUpdate();
            }
            try (PreparedStatement p = c.prepareStatement("INSERT INTO loan_repayments(loan_id,amount,principal_component,interest_component,payment_date) VALUES(?,1,1,0,'2026-01-01')")) {
                p.setInt(1, ids.get("loans")); p.executeUpdate();
            }
        }
        return ids;
    }

    private static int insert(Connection c, String sql, int userId, int accountId, String name) throws Exception {
        try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setInt(1, userId); p.setInt(2, accountId); p.setString(3, name); p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) { if (r.next()) return r.getInt(1); }
        }
        throw new AssertionError("SQLite did not return an inserted ID");
    }

    private static int insert(Connection c, String sql, int userId, String name) throws Exception {
        try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setInt(1, userId); p.setString(2, name); p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) { if (r.next()) return r.getInt(1); }
        }
        throw new AssertionError("SQLite did not return an inserted ID");
    }

    private static Reply send(HttpClient client, String base, String method, String path, String body, String cookie) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(base + path))
            .header("Origin", "http://localhost:5173")
            .header("Content-Type", "application/json");
        if (cookie != null) builder.header("Cookie", cookie);
        builder.method(method, body.isEmpty() ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        String setCookie = response.headers().firstValue("Set-Cookie").orElse("");
        String session = setCookie.isEmpty() ? null : setCookie.split(";", 2)[0];
        return new Reply(response.statusCode(), response.body(), session);
    }

    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError("FAIL: " + name);
        passed++;
        System.out.println("PASS: " + name);
    }
    private record Reply(int status, String body, String cookie) {}
}
