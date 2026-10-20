package com.dostatok.controller;

import com.dostatok.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.net.URI;

/**
 * Utility helper for HTTP handling, CORS configuration, and standard JSON responses.
 */
public final class HttpHelper {
    private HttpHelper() {
        // Prevent instantiation
    }

    public static void applyCors(HttpExchange exchange) {
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        if (isAllowedOrigin(origin)) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
            exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
            exchange.getResponseHeaders().set("Vary", "Origin");
        }
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
    }

    public static boolean isAllowedOrigin(String origin) {
        if (origin == null) return false;
        try {
            URI uri = URI.create(origin);
            String host = uri.getHost();
            return ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
                && ("localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host))
                && uri.getPort() == 5173;
        } catch (IllegalArgumentException e) { return false; }
    }

    public static boolean validMutationOrigin(HttpExchange exchange) {
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        return origin != null && isAllowedOrigin(origin);
    }

    public static String cookieValue(HttpExchange exchange, String name) {
        String cookie = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookie == null) return null;
        for (String item : cookie.split(";")) {
            String[] pair = item.trim().split("=", 2);
            if (pair.length == 2 && name.equals(pair[0])) return pair[1];
        }
        return null;
    }

    public static boolean handlePreflight(HttpExchange exchange) throws IOException {
        applyCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    public static String readRequestBody(HttpExchange exchange) throws IOException {
        Object cached = exchange.getAttribute("dostatok.body");
        if (cached instanceof String body) return body;
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = is.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
            String body = baos.toString(StandardCharsets.UTF_8);
            exchange.setAttribute("dostatok.body", body);
            return body;
        }
    }

    public static void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        applyCors(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", statusCode >= 200 && statusCode < 300);
        responseMap.put("data", data);

        byte[] bytes = JsonUtil.toJson(responseMap).getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        applyCors(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");

        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("success", false);
        errorMap.put("error", message);
        errorMap.put("statusCode", statusCode);

        byte[] bytes = JsonUtil.toJson(errorMap).getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}

