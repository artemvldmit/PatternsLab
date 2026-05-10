package com.example.shop;

import com.example.shop.db.DataSourceSingleton;
import com.example.shop.ws.ShopWebSocketServer;
import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws Exception {
        DataSourceSingleton.getInstance();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/ping", exchange -> {
            String resp = "pong";
            byte[] respBytes = resp.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, respBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(respBytes);
            os.close();
        });
        server.createContext("/products", new com.example.shop.http.ProductsHandler());
        server.createContext("/cart", new com.example.shop.http.CartHandler());
        server.createContext("/cart/add", new com.example.shop.http.CartHandler());
        server.createContext("/orders/create", new com.example.shop.http.OrdersHandler());
        server.createContext("/products/update-price", new com.example.shop.http.ProductPriceHandler());
        server.createContext("/subscriptions/add", new com.example.shop.http.SubscriptionHandler());
        server.createContext("/subscriptions/remove", new com.example.shop.http.SubscriptionHandler());
        server.createContext("/analytics", new com.example.shop.http.AnalyticsHandler());
        server.createContext("/users", new com.example.shop.http.UserHandler());
        server.start();
        System.out.println("HTTP server started on port 8080");

        ShopWebSocketServer ws = new ShopWebSocketServer(8081);
        ws.start();
        System.out.println("WebSocket server started on port 8081");
    }
}
