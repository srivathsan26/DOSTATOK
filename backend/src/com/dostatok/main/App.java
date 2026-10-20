package com.dostatok.main;

import com.dostatok.controller.ApiController;
import com.dostatok.dao.AccountRepository;
import com.dostatok.dao.AccountRepositoryImpl;
import com.dostatok.dao.FinanceRepository;
import com.dostatok.dao.FinanceRepositoryImpl;
import com.dostatok.dao.TransactionRepository;
import com.dostatok.dao.TransactionRepositoryImpl;
import com.dostatok.database.DatabaseConfig;
import com.dostatok.service.AccountService;
import com.dostatok.service.FinanceService;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Main application bootstrap for DOSTATOK — Personal Financial Management System.
 * Demonstrates:
 * - Clean multi-tiered architectural composition
 * - Inversion of Control / Dependency Injection
 * - Self-initializing SQLite persistent database
 * - Multi-threaded JDK built-in HTTP Server
 */
public final class App {
    private static final int PORT = Integer.getInteger("DOSTATOK_PORT", 8080);

    private App() {
        // Prevent instantiation
    }

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("   DOSTATOK — Personal Financial Management System        ");
        System.out.println("==========================================================");

        try {
            // 1. Initialize SQLite Database & Tables
            DatabaseConfig.initializeDatabase();
            System.out.println("Database storage: " + DatabaseConfig.getDatabasePath().toAbsolutePath());

            // 2. Instantiate Data Access (Repository) Layer
            AccountRepository accountRepository = new AccountRepositoryImpl();
            TransactionRepository transactionRepository = new TransactionRepositoryImpl();
            FinanceRepository financeRepository = new FinanceRepositoryImpl();

            // 3. Instantiate Business Logic (Service) Layer
            AccountService accountService = new AccountService(accountRepository, transactionRepository);
            FinanceService financeService = new FinanceService(
                financeRepository, accountRepository, transactionRepository, accountService
            );

            // 4. Instantiate API Controller
            ApiController apiController = new ApiController(accountService, financeService);

            // 5. Configure JDK HttpServer
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/api/", apiController);
            server.createContext("/api", apiController);

            // Set up multi-threaded thread pool for high throughput
            server.setExecutor(Executors.newFixedThreadPool(12));
            server.start();

            System.out.println("DOSTATOK HTTP Server listening at: http://localhost:" + PORT);
            System.out.println("Health check: http://localhost:" + PORT + "/api/health");
            System.out.println("Dashboard summary: http://localhost:" + PORT + "/api/dashboard/summary");
            System.out.println("Accounts endpoint: http://localhost:" + PORT + "/api/accounts");
            System.out.println("Ready to serve requests from React frontend at http://localhost:5173");
            System.out.println("==========================================================");

        } catch (IOException e) {
            System.err.println("Fatal: Failed to start HTTP server on port " + PORT + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Fatal: Application bootstrap error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
