package com.example.shop.analytics.model;

import java.util.List;
import java.util.Map;

public class AnalyticsReport {
    private final int orderCount;
    private final double averageOrderTotal;
    private final List<String> topProducts;
    private final Map<String, Integer> couponFrequency;

    public AnalyticsReport(int orderCount, double averageOrderTotal, List<String> topProducts, Map<String, Integer> couponFrequency) {
        this.orderCount = orderCount;
        this.averageOrderTotal = averageOrderTotal;
        this.topProducts = topProducts;
        this.couponFrequency = couponFrequency;
    }

    public int getOrderCount() { return orderCount; }
    public double getAverageOrderTotal() { return averageOrderTotal; }
    public List<String> getTopProducts() { return topProducts; }
    public Map<String, Integer> getCouponFrequency() { return couponFrequency; }
}
