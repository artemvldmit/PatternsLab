package com.example.shop.http;

import com.example.shop.patterns.facade.OrderFacade;
import com.example.shop.patterns.order.Order;
import com.example.shop.patterns.observer.OrderEventNotifier;
import com.example.shop.patterns.payment.CreditCardFactory;
import com.example.shop.patterns.delivery.CourierDelivery;
import com.example.shop.patterns.singleton.CartManager;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class OrdersHandler implements HttpHandler {
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        String method = exchange.getRequestMethod();

        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equals(method) && exchange.getRequestURI().getPath().endsWith("/orders/create")) {
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = gson.fromJson(body, Map.class);
            int userId = ((Number) data.getOrDefault("userId", 1)).intValue();

            Order.Builder b = new Order.Builder().userId(userId).status("NEW");
            CartManager.getInstance().getItems().forEach(b::addItem);
            Order order = b.build();

            OrderFacade facade = new OrderFacade(new CreditCardFactory(), new CourierDelivery());
            boolean ok = facade.placeOrder(order, Map.of());
            if (ok) {
                OrderEventNotifier.notifyAllClients(gson.toJson(Map.of("event", "order_created", "orderId", order.getId())));
                new java.util.ArrayList<>(CartManager.getInstance().getItems())
                    .forEach(i -> CartManager.getInstance().remove(i));
                String resp = gson.toJson(Map.of("status", "ok", "orderId", order.getId()));
                byte[] bresp = resp.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(200, bresp.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(bresp); }
            } else {
                exchange.sendResponseHeaders(500, -1);
            }
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }
}
