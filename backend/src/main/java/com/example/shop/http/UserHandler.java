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
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        try {
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = gson.fromJson(body, Map.class);
            String email = (String) data.getOrDefault("email", "");
            String role = (String) data.getOrDefault("role", "USER");

            if (email.isBlank()) {
                byte[] err = gson.toJson(Map.of("message", "email required"))
                        .getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(400, err.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(err); }
                return;
            }

            Map<String, Object> user = dao.loginOrCreate(email, role);
            if (user == null) {
                exchange.sendResponseHeaders(500, -1);
                return;
            }
            byte[] resp = gson.toJson(user).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(resp); }
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1);
        }
    }
}
