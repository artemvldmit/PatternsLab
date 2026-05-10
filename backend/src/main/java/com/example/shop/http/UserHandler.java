package com.example.shop.http;

import com.example.shop.dao.UserDAO;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UserHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private final UserDAO dao = new UserDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path   = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("GET".equals(method) && "/users".equals(path)) {
            handleGetAll(exchange);
            return;
        }

        if ("POST".equals(method) && path.endsWith("/login")) {
            handleLogin(exchange);
            return;
        }

        exchange.sendResponseHeaders(405, -1);
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        try {
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = gson.fromJson(body, Map.class);
            String email = (String) data.getOrDefault("email", "");
            String role  = (String) data.getOrDefault("role", "USER");

            if (email.isBlank()) {
                send(exchange, 400, Map.of("message", "email required"));
                return;
            }

            Map<String, Object> user = dao.loginOrCreate(email, role);
            if (user == null) { exchange.sendResponseHeaders(500, -1); return; }

            int status = "role_mismatch".equals(user.get("error")) ? 409 : 200;
            send(exchange, status, user);
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1);
        }
    }

    private void send(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] resp = gson.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, resp.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(resp); }
    }

    private void handleGetAll(HttpExchange exchange) throws IOException {
        try {
            send(exchange, 200, dao.getAllUsers());
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1);
        }
    }
}
