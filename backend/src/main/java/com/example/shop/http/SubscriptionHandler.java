package com.example.shop.http;

import com.example.shop.dao.SubscriptionDAO;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SubscriptionHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private final SubscriptionDAO dao = new SubscriptionDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (!"POST".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405,-1); return; }
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = gson.fromJson(body, Map.class);
        int userId = ((Number) data.getOrDefault("userId", 0)).intValue();
        int productId = ((Number) data.getOrDefault("productId", 0)).intValue();

        boolean ok = false;
        if (path.endsWith("/subscriptions/add")) ok = dao.addSubscription(userId, productId);
        else if (path.endsWith("/subscriptions/remove")) ok = dao.removeSubscription(userId, productId);

        if (ok) {
            byte[] resp = gson.toJson(Map.of("status","ok")).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type","application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, resp.length);
            OutputStream os = exchange.getResponseBody(); os.write(resp); os.close();
        } else {
            exchange.sendResponseHeaders(500,-1);
        }
    }
}
