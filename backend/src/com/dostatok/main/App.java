package com.dostatok.main;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public final class App {
    // This private constructor prevents creating an App object because the server uses only static setup.
    private App() {}

    // This method starts the HTTP server and reports any startup or request errors.
    public static void main(String[] args) throws IOException {
        // This server listens for local HTTP requests on port 8080.
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        // This handler responds only at the health-check URL.
        server.createContext("/api/health", exchange -> handleHealth(exchange));
        // This line starts accepting requests in the background.
        server.start();
        // This message tells the beginner where the server is running.
        System.out.println("DOSTATOK server running at http://localhost:8080");
    }

    // This method sends the health response with the requested headers and JSON body.
    private static void handleHealth(HttpExchange exchange) throws IOException {
        // This header allows the local React development site to call this server.
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "http://localhost:5173");
        // This header tells clients that the response contains UTF-8 JSON.
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        // This string is the exact health information returned to the caller.
        byte[] response = "{\"status\":\"ok\",\"app\":\"DOSTATOK\"}".getBytes(StandardCharsets.UTF_8);
        // This sends an HTTP success status and the body length before writing the body.
        exchange.sendResponseHeaders(200, response.length);
        // This resource block always closes the response stream after writing the body.
        try (OutputStream output = exchange.getResponseBody()) {
            // This writes the JSON bytes to the client.
            output.write(response);
        }
    }
}
