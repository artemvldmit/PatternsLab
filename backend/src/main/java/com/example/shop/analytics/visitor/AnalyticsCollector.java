package com.example.shop.analytics.visitor;

import com.example.shop.analytics.model.AnalyticsOrder;

import java.util.*;

public class AnalyticsCollector implements AnalyticsVisitor {
    private int orderCount;
    private double total;
    private final Map<String, Integer> productFrequency = new HashMap<>();
    private final Map<String, Integer> couponFrequency = new HashMap<>();

    @Override
    public void visit(AnalyticsOrder order) {
        orderCount++;
        total += order.getTotal();
        for (String product : order.getProducts()) {
            productFrequency.merge(product, 1, Integer::sum);
        }
        if (order.getCoupon() != null && !order.getCoupon().isBlank()) {
            couponFrequency.merge(order.getCoupon(), 1, Integer::sum);
        }
    }

    public int getOrderCount() { return orderCount; }
    public double getAverageOrderTotal() { return orderCount == 0 ? 0.0 : total / orderCount; }
    public List<String> getTopProducts() {
        return productFrequency.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
    }
    public Map<String, Integer> getCouponFrequency() { return couponFrequency; }
}
