package com.example.shop.http;

import com.example.shop.analytics.AnalyticsService;
import com.example.shop.analytics.model.AnalyticsReport;
import com.example.shop.analytics.proxy.AnalyticsProxy;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AnalyticsHandler implements HttpHandler {
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String role = exchange.getRequestURI().getQuery() != null && exchange.getRequestURI().getQuery().contains("role=ADMIN")
                ? "ADMIN"
                : exchange.getRequestHeaders().getFirst("X-Role");

        // Client works against AnalyticsService interface — transparent to whether proxy or real subject
        AnalyticsService service = new AnalyticsProxy(role == null ? "USER" : role);

        try {
            AnalyticsReport report = service.getAnalytics();
            byte[] resp = gson.toJson(Map.of("status", "ok", "report", report)).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        } catch (SecurityException ex) {
            byte[] resp = gson.toJson(Map.of("status", "forbidden", "message", ex.getMessage())).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(403, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        }
    }
}
