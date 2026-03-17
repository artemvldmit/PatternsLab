package com.example.shop.http;

import com.example.shop.patterns.factory.Product;
import com.example.shop.patterns.factory.ProductFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ProductsHandler implements HttpHandler {
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            List<Product> sample = new ArrayList<>();
            sample.add(ProductFactory.create("PHYSICAL", 1, "Chair", 49.99));
            sample.add(ProductFactory.create("DIGITAL", 2, "E-Book", 9.99));
            sample.add(ProductFactory.create("SUBSCRIPTION", 3, "Premium", 4.99));

            List<JsonObject> withType = new ArrayList<>();
            for (Product p : sample) {
                JsonObject obj = gson.toJsonTree(p).getAsJsonObject();
                obj.addProperty("type", p.getType());
                withType.add(obj);
            }

            String resp = gson.toJson(withType);
            byte[] b = resp.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, b.length);
            OutputStream os = exchange.getResponseBody();
            os.write(b);
            os.close();
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }
}

