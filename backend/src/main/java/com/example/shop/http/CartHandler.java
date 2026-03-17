package com.example.shop.http;

import com.example.shop.patterns.command.AddProductCommand;
import com.example.shop.patterns.command.CommandManager;
import com.example.shop.patterns.decorator.DiscountDecorator;
import com.example.shop.patterns.decorator.ExpressProcessingDecorator;
import com.example.shop.patterns.decorator.GiftWrapDecorator;
import com.example.shop.patterns.factory.Product;
import com.example.shop.patterns.factory.ProductFactory;
import com.example.shop.patterns.memento.CartCaretaker;
import com.example.shop.patterns.memento.CartMemento;
import com.example.shop.patterns.singleton.CartManager;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CartHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private final CommandManager cmdManager = new CommandManager();
    private final CartCaretaker caretaker = new CartCaretaker();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        String method = exchange.getRequestMethod();

        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equals(method) && exchange.getRequestURI().getPath().endsWith("/cart/add")) {
            handleAddToCart(exchange);
        } else if ("GET".equals(method) && exchange.getRequestURI().getPath().endsWith("/cart")) {
            handleGetCart(exchange);
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }

    private void handleAddToCart(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = gson.fromJson(body, Map.class);

        String type = (String) data.getOrDefault("type", "PHYSICAL");
        double price = ((Number) data.getOrDefault("price", 0)).doubleValue();
        int id = ((Number) data.getOrDefault("id", 0)).intValue();
        String name = (String) data.getOrDefault("name", "Item");

        Product p = ProductFactory.create(type, id, name, price);

        // Apply decorators based on optional request flags
        if (Boolean.TRUE.equals(data.get("giftWrap"))) {
            p = new GiftWrapDecorator(p);
        }
        if (Boolean.TRUE.equals(data.get("express"))) {
            p = new ExpressProcessingDecorator(p);
        }
        Number discountNumber = (Number) data.get("discount");
        if (discountNumber != null && discountNumber.doubleValue() > 0) {
            p = new DiscountDecorator(p, discountNumber.doubleValue());
        }

        caretaker.save(new CartMemento(gson.toJson(serializeItems(CartManager.getInstance().getItems()))));
        cmdManager.execute(new AddProductCommand(p));

        sendJson(exchange, 200, Map.of("status", "ok", "price", p.getPrice()));
    }

    private void handleGetCart(HttpExchange exchange) throws IOException {
        List<Map<String, Object>> items = serializeItems(CartManager.getInstance().getItems());
        sendJson(exchange, 200, items);
    }

    private List<Map<String, Object>> serializeItems(List<Product> products) {
        return products.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("price", p.getPrice());
            m.put("type", p.getType());
            return m;
        }).collect(Collectors.toList());
    }

    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] b = gson.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, b.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(b);
        }
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }
}
