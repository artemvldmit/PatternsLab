package com.example.shop.analytics.model;

import com.example.shop.analytics.visitor.AnalyticsVisitor;

import java.util.List;

public class AnalyticsOrder {
    private final int id;
    private final double total;
    private final String coupon;
    private final List<String> products;

    public AnalyticsOrder(int id, double total, String coupon, List<String> products) {
        this.id = id;
        this.total = total;
        this.coupon = coupon;
        this.products = products;
    }

    public int getId() { return id; }
    public double getTotal() { return total; }
    public String getCoupon() { return coupon; }
    public List<String> getProducts() { return products; }

    // Visitor pattern: double-dispatch — element calls visitor back with itself
    public void accept(AnalyticsVisitor v) { v.visit(this); }
}
