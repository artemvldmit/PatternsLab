package com.example.shop.http;

import com.example.shop.dao.ProductDAO;
import com.example.shop.dao.SubscriptionDAO;
import com.example.shop.patterns.observer.OrderEventNotifier;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ProductPriceHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private final ProductDAO productDAO = new ProductDAO();
    private final SubscriptionDAO subDAO = new SubscriptionDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405,-1); return; }
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = gson.fromJson(body, Map.class);
        int productId = ((Number) data.getOrDefault("productId", 0)).intValue();
        double newPrice = ((Number) data.getOrDefault("price", 0)).doubleValue();

        Double oldPrice = productDAO.getPriceById(productId);
        boolean ok = productDAO.updatePrice(productId, newPrice);
        if (ok) {
            List<Integer> subs = subDAO.getSubscribersByProduct(productId);
            String event = gson.toJson(Map.of(
                    "event","price_updated",
                    "productId", productId,
                    "oldPrice", oldPrice,
                    "newPrice", newPrice,
                    "subscribers", subs
            ));
            OrderEventNotifier.notifyAllClients(event);
            byte[] resp = gson.toJson(Map.of("status","ok")).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type","application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, resp.length);
            OutputStream os = exchange.getResponseBody(); os.write(resp); os.close();
        } else {
            exchange.sendResponseHeaders(500,-1);
        }
    }
}
