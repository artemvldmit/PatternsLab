package com.example.shop.patterns.factory;

public class SubscriptionProduct extends Product {
    public SubscriptionProduct(int id, String name, double price) {
        super(id, name, price);
    }

    @Override
    public String getType() {
        return "SUBSCRIPTION";
    }
}
