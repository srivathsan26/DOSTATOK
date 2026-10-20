package com.dostatok.controller;

import com.dostatok.exception.DostatokException;
import com.dostatok.model.*;
import com.dostatok.service.AccountService;
import com.dostatok.service.FinanceService;
import com.dostatok.service.AuthService;
import com.dostatok.service.SessionStore;
import com.dostatok.util.CurrencyUtil;
import com.dostatok.util.JsonUtil;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.ArrayDeque;
import java.time.Instant;

/**
 * Main REST Controller handling /api/* routes.
 * Decouples HTTP protocol concerns from underlying business services.
 */
public class ApiController implements HttpHandler {
    private final AccountService accountService;
    private final FinanceService financeService;
    private final AuthService authService = new AuthService();
    private final SessionStore sessions = new SessionStore();
    private final ConcurrentMap<String, ArrayDeque<Instant>> loginAttempts = new ConcurrentHashMap<>();

    public ApiController(AccountService accountService, FinanceService financeService) {
        this.accountService = accountService;
        this.financeService = financeService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpHelper.handlePreflight(exchange)) {
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod().toUpperCase();

        try {
            if (path.startsWith("/api/auth/")) {
                handleAuth(exchange, path, method);
                return;
            }
            // Health Endpoint
            if ("/api/health".equals(path)) {
                Map<String, Object> health = new HashMap<>();
                health.put("status", "ok");
                health.put("app", "DOSTATOK - Personal Financial Management System");
                health.put("version", "1.0.0");
                HttpHelper.sendJsonResponse(exchange, 200, health);
                return;
            }

            if (!"GET".equals(method) && !"HEAD".equals(method) && !HttpHelper.validMutationOrigin(exchange)) {
                HttpHelper.sendErrorResponse(exchange, 403, "Request origin is not allowed.");
                return;
            }
            Integer authenticatedUser = sessions.resolve(HttpHelper.cookieValue(exchange, SessionStore.COOKIE_NAME));
            if (authenticatedUser == null) {
                HttpHelper.sendErrorResponse(exchange, 401, "Please sign in to continue.");
                return;
            }
            exchange.setAttribute("dostatok.userId", authenticatedUser);
            if (!ownsPathResource(exchange, path, authenticatedUser)) {
                HttpHelper.sendErrorResponse(exchange, 404, "Resource not found.");
                return;
            }
            if (!"GET".equals(method) && !"HEAD".equals(method)) {
                String body = HttpHelper.readRequestBody(exchange);
                if (!body.isBlank()) {
                    JsonObject payload = JsonUtil.parseObject(body);
                    for (String key : new String[]{"accountId", "fromAccountId", "toAccountId"}) {
                        int accountId = getAsInt(payload, key);
                        if (accountId > 0 && !ownsResource("bank_accounts", accountId, authenticatedUser)) {
                            HttpHelper.sendErrorResponse(exchange, 404, "Resource not found.");
                            return;
                        }
                    }
                }
            }

            // Dashboard Summary
            if ("/api/dashboard/summary".equals(path) && "GET".equals(method)) {
                DashboardSummary summary = financeService.getDashboardSummary(userId(exchange));
                HttpHelper.sendJsonResponse(exchange, 200, summary);
                return;
            }

            // Accounts Routing
            if (path.startsWith("/api/accounts")) {
                handleAccounts(exchange, path, method);
                return;
            }

            // Transactions Routing
            if ("/api/transactions".equals(path) && "GET".equals(method)) {
                List<FinancialTransaction> list = accountService.getAllTransactions(userId(exchange), 100);
                HttpHelper.sendJsonResponse(exchange, 200, list);
                return;
            }

            // Income Routing
            if (path.startsWith("/api/income")) {
                handleIncome(exchange, path, method);
                return;
            }

            // Expense Routing
            if (path.startsWith("/api/expenses")) {
                handleExpenses(exchange, path, method);
                return;
            }

            // Investments Routing
            if (path.startsWith("/api/investments")) {
                handleInvestments(exchange, path, method);
                return;
            }

            // Assets Routing
            if (path.startsWith("/api/assets")) {
                handleAssets(exchange, path, method);
                return;
            }

            // Loans Routing
            if (path.startsWith("/api/loans")) {
                handleLoans(exchange, path, method);
                return;
            }

            // Goals Routing
            if (path.startsWith("/api/goals")) {
                handleGoals(exchange, path, method);
                return;
            }

            // Not Found
            HttpHelper.sendErrorResponse(exchange, 404, "Endpoint not found: " + method + " " + path);
        } catch (DostatokException e) {
            HttpHelper.sendErrorResponse(exchange, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            System.err.println("Request failed: " + method + " " + path + " (" + e.getClass().getSimpleName() + ")");
            HttpHelper.sendErrorResponse(exchange, 500, "An internal server error occurred.");
        }
    }

    private void handleAuth(HttpExchange exchange, String path, String method) throws Exception {
        String sessionId = HttpHelper.cookieValue(exchange, SessionStore.COOKIE_NAME);
        if ("GET".equals(method) && "/api/auth/me".equals(path)) {
            Integer userId = sessions.resolve(sessionId);
            if (userId == null) { HttpHelper.sendErrorResponse(exchange, 401, "Please sign in to continue."); return; }
            AuthService.Profile profile = authService.getProfile(userId);
            if (profile == null) { sessions.invalidate(sessionId); HttpHelper.sendErrorResponse(exchange, 401, "Please sign in to continue."); return; }
            HttpHelper.sendJsonResponse(exchange, 200, profile);
            return;
        }
        if ("PUT".equals(method) && "/api/auth/profile".equals(path)) {
            Integer userId = sessions.resolve(sessionId);
            if (userId == null) { HttpHelper.sendErrorResponse(exchange, 401, "Please sign in to continue."); return; }
            if (!HttpHelper.validMutationOrigin(exchange)) { HttpHelper.sendErrorResponse(exchange, 403, "Request origin is not allowed."); return; }
            JsonObject json = JsonUtil.parseObject(HttpHelper.readRequestBody(exchange));
            try {
                AuthService.Profile profile = authService.updateProfile(userId, getAsInt(json, "age"),
                        getAsString(json, "occupation"), getAsString(json, "address"), getAsBigDecimal(json, "annualIncome"));
                if (profile == null) { HttpHelper.sendErrorResponse(exchange, 401, "Please sign in to continue."); return; }
                HttpHelper.sendJsonResponse(exchange, 200, profile);
            } catch (IllegalArgumentException e) {
                HttpHelper.sendErrorResponse(exchange, 400, e.getMessage());
            }
            return;
        }
        if (!"POST".equals(method) || !("/api/auth/register".equals(path) || "/api/auth/login".equals(path) || "/api/auth/logout".equals(path))) {
            HttpHelper.sendErrorResponse(exchange, 404, "Endpoint not found."); return;
        }
        if (!HttpHelper.validMutationOrigin(exchange)) { HttpHelper.sendErrorResponse(exchange, 403, "Request origin is not allowed."); return; }
        if ("/api/auth/logout".equals(path)) {
            sessions.invalidate(sessionId);
            setSessionCookie(exchange, "", 0);
            HttpHelper.sendJsonResponse(exchange, 200, Map.of("message", "Signed out."));
            return;
        }
        String remote = exchange.getRemoteAddress().getAddress().getHostAddress();
        if (!allowLoginAttempt(remote)) { HttpHelper.sendErrorResponse(exchange, 429, "Too many attempts. Wait 15 minutes and try again."); return; }
        JsonObject json = JsonUtil.parseObject(HttpHelper.readRequestBody(exchange));
        String email = getAsString(json, "email");
        String rawPassword = getAsString(json, "password");
        char[] password = rawPassword == null ? new char[0] : rawPassword.toCharArray();
        AuthService.Profile profile;
        try {
            if ("/api/auth/register".equals(path)) {
                profile = authService.register(getAsString(json, "name"), email, password);
            } else {
                profile = authService.authenticate(email, password);
                if (profile == null) { HttpHelper.sendErrorResponse(exchange, 401, "Email or password is incorrect."); return; }
            }
            sessions.invalidate(sessionId);
            String newSession = sessions.create(profile.id());
            setSessionCookie(exchange, newSession, sessions.maxAgeSeconds());
            HttpHelper.sendJsonResponse(exchange, "/api/auth/register".equals(path) ? 201 : 200, profile);
        } catch (IllegalArgumentException e) {
            HttpHelper.sendErrorResponse(exchange, 400, e.getMessage());
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    private boolean allowLoginAttempt(String key) {
        ArrayDeque<Instant> attempts = loginAttempts.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (attempts) {
            Instant cutoff = Instant.now().minusSeconds(900);
            while (!attempts.isEmpty() && attempts.peekFirst().isBefore(cutoff)) attempts.removeFirst();
            if (attempts.size() >= 10) return false;
            attempts.addLast(Instant.now());
            return true;
        }
    }

    private void setSessionCookie(HttpExchange exchange, String value, long maxAge) {
        // Set this JVM property when HTTPS terminates in front of the local HTTP listener.
        boolean secure = Boolean.getBoolean("DOSTATOK_SECURE_COOKIES");
        String cookie = SessionStore.COOKIE_NAME + "=" + value + "; Path=/; HttpOnly; SameSite=Strict; Max-Age=" + maxAge;
        if (secure) cookie += "; Secure";
        exchange.getResponseHeaders().add("Set-Cookie", cookie);
    }

    private int userId(HttpExchange exchange) {
        Object value = exchange.getAttribute("dostatok.userId");
        return value instanceof Integer id ? id : -1;
    }

    private boolean ownsPathResource(HttpExchange exchange, String path, int ownerId) throws Exception {
        String[] parts = path.split("/");
        if (parts.length < 4) return true;
        String table = switch (parts[2]) {
            case "accounts" -> "bank_accounts";
            case "income" -> "income";
            case "expenses" -> "expenses";
            case "investments" -> "investments";
            case "assets" -> "assets";
            case "loans" -> "loans";
            case "goals" -> "financial_goals";
            default -> null;
        };
        if (table == null || "transfer".equals(parts[3])) return true;
        try {
            int resourceId = Integer.parseInt(parts[3]);
            return ownsResource(table, resourceId, ownerId);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean ownsResource(String table, int id, int ownerId) throws Exception {
        if (!java.util.Set.of("bank_accounts", "income", "expenses", "investments", "assets", "loans", "financial_goals").contains(table)) return false;
        try (java.sql.Connection connection = com.dostatok.dao.DBConnection.getConnection();
             java.sql.PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM " + table + " WHERE id = ? AND user_id = ?")) {
            statement.setInt(1, id);
            statement.setInt(2, ownerId);
            try (java.sql.ResultSet result = statement.executeQuery()) { return result.next(); }
        }
    }

    // ==================== ACCOUNT HANDLERS ====================
    private void handleAccounts(HttpExchange exchange, String path, String method) throws Exception {
        String body = HttpHelper.readRequestBody(exchange);

        if ("/api/accounts".equals(path)) {
            if ("GET".equals(method)) {
                List<BankAccount> accounts = accountService.getUserAccounts(userId(exchange));
                HttpHelper.sendJsonResponse(exchange, 200, accounts);
                return;
            } else if ("POST".equals(method)) {
                JsonObject json = JsonUtil.parseObject(body);
                BankAccount created = accountService.createAccount(
                    userId(exchange),
                    getAsString(json, "accountNumber"),
                    getAsString(json, "accountName"),
                    getAsString(json, "accountType"),
                    getAsString(json, "bankName"),
                    getAsBigDecimal(json, "initialBalance"),
                    getAsBigDecimal(json, "minimumBalance"),
                    getAsBigDecimal(json, "overdraftLimit"),
                    getAsBigDecimal(json, "interestRate")
                );
                HttpHelper.sendJsonResponse(exchange, 201, created);
                return;
            }
        }

        if ("/api/accounts/transfer".equals(path) && "POST".equals(method)) {
            JsonObject json = JsonUtil.parseObject(body);
            int fromId = getAsInt(json, "fromAccountId");
            int toId = getAsInt(json, "toAccountId");
            BigDecimal amount = getAsBigDecimal(json, "amount");
            String desc = getAsString(json, "description");
            accountService.transfer(fromId, toId, amount, desc);
            HttpHelper.sendJsonResponse(exchange, 200, Map.of("message", "Funds transferred successfully"));
            return;
        }

        // Patterns: /api/accounts/{id}, /api/accounts/{id}/deposit, /api/accounts/{id}/withdraw
        String[] parts = path.split("/");
        if (parts.length >= 4) {
            int accountId = Integer.parseInt(parts[3]);

            if (parts.length == 4) {
                if ("GET".equals(method)) {
                    BankAccount account = accountService.getAccountById(accountId);
                    HttpHelper.sendJsonResponse(exchange, 200, account);
                    return;
                } else if ("PUT".equals(method)) {
                    JsonObject json = JsonUtil.parseObject(body);
                    accountService.updateAccount(
                        accountId,
                        getAsString(json, "accountName"),
                        getAsString(json, "bankName"),
                        getAsBigDecimal(json, "minimumBalance"),
                        getAsBigDecimal(json, "overdraftLimit"),
                        getAsBigDecimal(json, "interestRate"),
                        getAsString(json, "status")
                    );
                    HttpHelper.sendJsonResponse(exchange, 200, Map.of("message", "Account updated successfully"));
                    return;
                } else if ("DELETE".equals(method)) {
                    accountService.deleteAccount(accountId);
                    HttpHelper.sendJsonResponse(exchange, 200, Map.of("message", "Account deleted successfully"));
                    return;
                }
            } else if (parts.length == 5) {
                String action = parts[4];
                JsonObject json = JsonUtil.parseObject(body);
                BigDecimal amount = getAsBigDecimal(json, "amount");
                String category = getAsString(json, "category");
                String desc = getAsString(json, "description");

                if ("deposit".equals(action) && "POST".equals(method)) {
                    FinancialTransaction tx = accountService.deposit(accountId, amount, category, desc);
                    HttpHelper.sendJsonResponse(exchange, 200, tx);
                    return;
                } else if ("withdraw".equals(action) && "POST".equals(method)) {
                    FinancialTransaction tx = accountService.withdraw(accountId, amount, category, desc);
                    HttpHelper.sendJsonResponse(exchange, 200, tx);
                    return;
                } else if ("transactions".equals(action) && "GET".equals(method)) {
                    List<FinancialTransaction> txList = accountService.getAccountTransactions(accountId);
                    HttpHelper.sendJsonResponse(exchange, 200, txList);
                    return;
                }
            }
        }

        HttpHelper.sendErrorResponse(exchange, 404, "Invalid account route: " + path);
    }

    // ==================== INCOME HANDLERS ====================
    private void handleIncome(HttpExchange exchange, String path, String method) throws Exception {
        String body = HttpHelper.readRequestBody(exchange);

        if ("/api/income".equals(path)) {
            if ("GET".equals(method)) {
                List<Income> list = financeService.getIncomeList(userId(exchange));
                HttpHelper.sendJsonResponse(exchange, 200, list);
                return;
            } else if ("POST".equals(method)) {
                JsonObject json = JsonUtil.parseObject(body);
                Income inc = new Income();
                inc.setUserId(userId(exchange));
                inc.setTitle(getAsString(json, "title"));
                inc.setAmount(getAsBigDecimal(json, "amount"));
                inc.setCategory(getAsString(json, "category"));
                inc.setSource(getAsString(json, "source"));
                inc.setIncomeDate(getAsString(json, "incomeDate"));
                inc.setNotes(getAsString(json, "notes"));
                if (json.has("accountId") && !json.get("accountId").isJsonNull()) {
                    inc.setAccountId(json.get("accountId").getAsInt());
                }
                boolean updateAccount = json.has("updateAccount") && json.get("updateAccount").getAsBoolean();
                Income saved = financeService.addIncome(inc, updateAccount);
                HttpHelper.sendJsonResponse(exchange, 201, saved);
                return;
            }
        }

        String[] parts = path.split("/");
        if (parts.length == 4) {
            int id = Integer.parseInt(parts[3]);
            if ("PUT".equals(method)) {
                JsonObject json = JsonUtil.parseObject(body);
                Income inc = new Income();
                inc.setId(id);
                inc.setUserId(userId(exchange));
                inc.setTitle(getAsString(json, "title"));
                inc.setAmount(getAsBigDecimal(json, "amount"));
                inc.setCategory(getAsString(json, "category"));
                inc.setSource(getAsString(json, "source"));
                inc.setIncomeDate(getAsString(json, "incomeDate"));
                inc.setNotes(getAsString(json, "notes"));
                if (json.has("accountId") && !json.get("accountId").isJsonNull()) {
                    inc.setAccountId(json.get("accountId").getAsInt());
                }
                financeService.updateIncome(inc);
                HttpHelper.sendJsonResponse(exchange, 200, Map.of("message", "Income updated successfully"));
                return;
            } else if ("DELETE".equals(method)) {
                financeService.deleteIncome(id);
                HttpHelper.sendJsonResponse(exchange, 200, Map.of("message", "Income deleted successfully"));
                return;
            }
        }

        HttpHelper.sendErrorResponse(exchange, 404, "Invalid income route: " + path);
    }

    // ==================== EXPENSE HANDLERS ====================
    private void handleExpenses(HttpExchange exchange, String path, String method) throws Exception {
        String body = HttpHelper.readRequestBody(exchange);

        if ("/api/expenses".equals(path)) {
            if ("GET".equals(method)) {
                List<Expense> list = financeService.getExpenseList(userId(exchange));
                HttpHelper.sendJsonResponse(exchange, 200, list);
                return;
            } else if ("POST".equals(method)) {
                JsonObject json = JsonUtil.parseObject(body);
                Expense exp = new Expense();
                exp.setUserId(userId(exchange));
                exp.setTitle(getAsString(json, "title"));
                exp.setAmount(getAsBigDecimal(json, "amount"));
                exp.setCategory(getAsString(json, "category"));
                exp.setPaymentMethod(getAsString(json, "paymentMethod"));
                exp.setExpenseDate(getAsString(json, "expenseDate"));
                exp.setNotes(getAsString(json, "notes"));
                if (json.has("accountId") && !json.get("accountId").isJsonNull()) {
                    exp.setAccountId(json.get("accountId").getAsInt());
                }
                boolean deduct = json.has("deductFromAccount") && json.get("deductFromAccount").getAsBoolean();
                Expense saved = financeService.addExpense(exp, deduct);
                HttpHelper.sendJsonResponse(exchange, 201, saved);
                return;
            }
        }

        String[] parts = path.split("/");
        if (parts.length == 4) {
            int id = Integer.parseInt(parts[3]);
            if ("PUT".equals(method)) {
                JsonObject json = JsonUtil.parseObject(body);
                Expense exp = new Expense();
                exp.setId(id);
                exp.setUserId(userId(exchange));
                exp.setTitle(getAsString(json, "title"));
                exp.setAmount(getAsBigDecimal(json, "amount"));
                exp.setCategory(getAsString(json, "category"));
                exp.setPaymentMethod(getAsString(json, "paymentMethod"));
                exp.setExpenseDate(getAsString(json, "expenseDate"));
                exp.setNotes(getAsString(json, "notes"));
                if (json.has("accountId") && !json.get("accountId").isJsonNull()) {
                    exp.setAccountId(json.get("accountId").getAsInt());
                }
                financeService.updateExpense(exp);
                HttpHelper.sendJsonResponse(exchange, 200, Map.of("message", "Expense updated successfully"));
                return;
            } else if ("DELETE".equals(method)) {
                financeService.deleteExpense(id);
                HttpHelper.sendJsonResponse(exchange, 200, Map.of("message", "Expense deleted successfully"));
                return;
            }
        }

        HttpHelper.sendErrorResponse(exchange, 404, "Invalid expense route: " + path);
    }

    // ==================== INVESTMENTS HANDLERS ====================
    private void handleInvestments(HttpExchange exchange, String path, String method) throws Exception {
        String body = HttpHelper.readRequestBody(exchange);

        if ("/api/investments".equals(path)) {
            if ("GET".equals(method)) {
                List<Investment> list = financeService.getInvestments(userId(exchange));
                HttpHelper.sendJsonResponse(exchange, 200, list);
                return;
            } else if ("POST".equals(method)) {
                JsonObject json = JsonUtil.parseObject(body);
                Investment inv = new Investment();
                inv.setUserId(userId(exchange));
                inv.setName(getAsString(json, "name"));
                inv.setInvestmentType(getAsString(json, "investmentType"));
                inv.setInvestedAmount(getAsBigDecimal(json, "investedAmount"));
                inv.setCurrentValue(getAsBigDecimal(json, "currentValue"));
                inv.setPurchaseDate(getAsString(json, "purchaseDate"));
                inv.setNotes(getAsString(json, "notes"));
                Investment saved = financeService.addInvestment(inv);
                HttpHelper.sendJsonResponse(exchange, 201, saved);
                return;
            }
        }

        String[] parts = path.split("/");
        if (parts.length == 4) {
            int id = Integer.parseInt(parts[3]);
            if ("PUT".equals(method)) {
                JsonObject json = JsonUtil.parseObject(body);
                Investment inv = new Investment();
                inv.setId(id);
                inv.setUserId(userId(exchange));
                inv.setName(getAsString(json, "name"));
                inv.setInvestmentType(getAsString(json, "investmentType"));
                inv.setInvestedAmount(getAsBigDecimal(json, "investedAmount"));
                inv.setCurrentValue(getAsBigDecimal(json, "currentValue"));
                inv.setPurchaseDate(getAsString(json, "purchaseDate"));
                inv.setNotes(getAsString(json, "notes"));
                financeService.updateInvestment(inv);
                HttpHelper.sendJsonResponse(exchange, 200, Map.of("message", "Investment updated successfully"));
                return;
            } else if ("DELETE".equals(method)) {
                financeService.deleteInvestment(id);
                HttpHelper.sendJsonResponse(exchange, 200, Map.of("message", "Investment deleted successfully"));
                return;
            }
        }

        HttpHelper.sendErrorResponse(exchange, 404, "Invalid investment route: " + path);
    }

    // ==================== ASSETS HANDLERS ====================
    private void handleAssets(HttpExchange exchange, String path, String method) throws Exception {
        String body = HttpHelper.readRequestBody(exchange);

        if ("/api/assets".equals(path)) {
            if ("GET".equals(method)) {
                List<Asset> list = financeService.getAssets(userId(exchange));
                HttpHelper.sendJsonResponse(exchange, 200, list);
                return;
            } else if ("POST".equals(method)) {
                JsonObject json = JsonUtil.parseObject(body);
                Asset asset = new Asset();
                asset.setUserId(userId(exchange));
                asset.setName(getAsString(json, "name"));
                asset.setAssetCategory(getAsString(json, "assetCategory"));
                asset.setPurchaseValue(getAsBigDecimal(json, "purchaseValue"));
                asset.setCurrentValue(getAsBigDecimal(json, "currentValue"));
                asset.setAcquisitionDate(getAsString(json, "acquisitionDate"));
                asset.setNotes(getAsString(json, "notes"));
                Asset saved = financeService.addAsset(asset);
                HttpHelper.sendJsonResponse(exchange, 201, saved);
                return;
            }
        }

        String[] parts = path.split("/");
        if (parts.length == 4) {
            int id = Integer.parseInt(parts[3]);
            if ("PUT".equals(method)) {
                JsonObject json = JsonUtil.parseObject(body);
                Asset asset = new Asset();
                asset.setId(id);
                asset.setUserId(userId(exchange));
                asset.setName(getAsString(json, "name"));
                asset.setAssetCategory(getAsString(json, "assetCategory"));
                asset.setPurchaseValue(getAsBigDecimal(json, "purchaseValue"));
                asset.setCurrentValue(getAsBigDecimal(json, "currentValue"));
                asset.setAcquisitionDate(getAsString(json, "acquisitionDate"));
                asset.setNotes(getAsString(json, "notes"));
                financeService.updateAsset(asset);
                HttpHelper.sendJsonResponse(exchange, 200, Map.of("message", "Asset updated successfully"));
                return;
            } else if ("DELETE".equals(method)) {
                financeService.deleteAsset(id);
                HttpHelper.sendJsonResponse(exchange, 200, Map.of("message", "Asset deleted successfully"));
                return;
            }
        }

        HttpHelper.sendErrorResponse(exchange, 404, "Invalid asset route: " + path);
    }

    // ==================== LOANS HANDLERS ====================
    private void handleLoans(HttpExchange exchange, String path, String method) throws Exception {
        String body = HttpHelper.readRequestBody(exchange);

        if ("/api/loans".equals(path)) {
            if ("GET".equals(method)) {
                List<Loan> list = financeService.getLoans(userId(exchange));
                HttpHelper.sendJsonResponse(exchange, 200, list);
                return;
            } else if ("POST".equals(method)) {
                JsonObject json = JsonUtil.parseObject(body);
                Loan loan = new Loan();
                loan.setUserId(userId(exchange));
                loan.setLoanName(getAsString(json, "loanName"));
                loan.setLender(getAsString(json, "lender"));
                loan.setPrincipalAmount(getAsBigDecimal(json, "principalAmount"));
                loan.setInterestRate(getAsBigDecimal(json, "interestRate"));
                loan.setTenureMonths(getAsInt(json, "tenureMonths"));
                loan.setStartDate(getAsString(json, "startDate"));
                loan.setOutstandingBalance(getAsBigDecimal(json, "outstandingBalance"));
                loan.setMonthlyEmi(getAsBigDecimal(json, "monthlyEmi"));
                Loan saved = financeService.addLoan(loan);
                HttpHelper.sendJsonResponse(exchange, 201, saved);
                return;
            }
        }

        String[] parts = path.split("/");
        if (parts.length >= 4) {
            int id = Integer.parseInt(parts[3]);

            if (parts.length == 4) {
                if ("PUT".equals(method)) {
                    JsonObject json = JsonUtil.parseObject(body);
                    Loan loan = new Loan();
                    loan.setId(id);
                    loan.setUserId(userId(exchange));
                    loan.setLoanName(getAsString(json, "loanName"));
                    loan.setLender(getAsString(json, "lender"));
                    loan.setPrincipalAmount(getAsBigDecimal(json, "principalAmount"));
                    loan.setInterestRate(getAsBigDecimal(json, "interestRate"));
                    loan.setTenureMonths(getAsInt(json, "tenureMonths"));
                    loan.setStartDate(getAsString(json, "startDate"));
                    loan.setOutstandingBalance(getAsBigDecimal(json, "outstandingBalance"));
                    loan.setMonthlyEmi(getAsBigDecimal(json, "monthlyEmi"));
                    loan.setStatus(getAsString(json, "status"));
                    financeService.updateLoan(loan);
                    HttpHelper.sendJsonResponse(exchange, 200, Map.of("message", "Loan updated successfully"));
                    return;
                } else if ("DELETE".equals(method)) {
                    financeService.deleteLoan(id);
                    HttpHelper.sendJsonResponse(exchange, 200, Map.of("message", "Loan deleted successfully"));
                    return;
                }
            } else if (parts.length == 5) {
                String action = parts[4];
                if ("repay".equals(action) && "POST".equals(method)) {
                    JsonObject json = JsonUtil.parseObject(body);
                    BigDecimal amount = getAsBigDecimal(json, "amount");
                    Integer accId = json.has("accountId") && !json.get("accountId").isJsonNull() ?
                            json.get("accountId").getAsInt() : null;
                    String notes = getAsString(json, "notes");
                    financeService.recordRepayment(id, accId, amount, notes);
                    HttpHelper.sendJsonResponse(exchange, 200, Map.of("message", "Loan repayment recorded successfully"));
                    return;
                } else if ("repayments".equals(action) && "GET".equals(method)) {
                    List<LoanRepayment> repayments = financeService.getLoanRepayments(id);
                    HttpHelper.sendJsonResponse(exchange, 200, repayments);
                    return;
                }
            }
        }

        HttpHelper.sendErrorResponse(exchange, 404, "Invalid loan route: " + path);
    }

    // ==================== GOALS HANDLERS ====================
    private void handleGoals(HttpExchange exchange, String path, String method) throws Exception {
        String body = HttpHelper.readRequestBody(exchange);

        if ("/api/goals".equals(path)) {
            if ("GET".equals(method)) {
                List<FinancialGoal> list = financeService.getGoals(userId(exchange));
                HttpHelper.sendJsonResponse(exchange, 200, list);
                return;
            } else if ("POST".equals(method)) {
                JsonObject json = JsonUtil.parseObject(body);
                FinancialGoal goal = new FinancialGoal();
                goal.setUserId(userId(exchange));
                goal.setTitle(getAsString(json, "title"));
                goal.setTargetAmount(getAsBigDecimal(json, "targetAmount"));
                goal.setCurrentAmount(getAsBigDecimal(json, "currentAmount"));
                goal.setTargetDate(getAsString(json, "targetDate"));
                goal.setCategory(getAsString(json, "category"));
                FinancialGoal saved = financeService.addGoal(goal);
                HttpHelper.sendJsonResponse(exchange, 201, saved);
                return;
            }
        }

        String[] parts = path.split("/");
        if (parts.length >= 4) {
            int id = Integer.parseInt(parts[3]);

            if (parts.length == 4) {
                if ("PUT".equals(method)) {
                    JsonObject json = JsonUtil.parseObject(body);
                    FinancialGoal goal = new FinancialGoal();
                    goal.setId(id);
                    goal.setUserId(userId(exchange));
                    goal.setTitle(getAsString(json, "title"));
                    goal.setTargetAmount(getAsBigDecimal(json, "targetAmount"));
                    goal.setCurrentAmount(getAsBigDecimal(json, "currentAmount"));
                    goal.setTargetDate(getAsString(json, "targetDate"));
                    goal.setCategory(getAsString(json, "category"));
                    goal.setStatus(getAsString(json, "status"));
                    financeService.updateGoal(goal);
                    HttpHelper.sendJsonResponse(exchange, 200, Map.of("message", "Goal updated successfully"));
                    return;
                } else if ("DELETE".equals(method)) {
                    financeService.deleteGoal(id);
                    HttpHelper.sendJsonResponse(exchange, 200, Map.of("message", "Goal deleted successfully"));
                    return;
                }
            } else if (parts.length == 5 && "contribute".equals(parts[4]) && "POST".equals(method)) {
                JsonObject json = JsonUtil.parseObject(body);
                BigDecimal amount = getAsBigDecimal(json, "amount");
                Integer accId = json.has("accountId") && !json.get("accountId").isJsonNull() ?
                        json.get("accountId").getAsInt() : null;
                financeService.contributeToGoal(id, accId, amount);
                HttpHelper.sendJsonResponse(exchange, 200, Map.of("message", "Contribution applied successfully"));
                return;
            }
        }

        HttpHelper.sendErrorResponse(exchange, 404, "Invalid goal route: " + path);
    }

    // Helper parsers for JsonObject
    private String getAsString(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsString();
        }
        return null;
    }

    private BigDecimal getAsBigDecimal(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return CurrencyUtil.parseAmount(json.get(key).getAsString());
        }
        return null;
    }

    private int getAsInt(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsInt();
        }
        return 0;
    }
}

