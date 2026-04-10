package com.example.shop.patterns.factory;

public class SubscriptionProductCreator extends ProductCreator {
    @Override
    public Product createProduct(int id, String name, double price) {
        return new SubscriptionProduct(id, name, price);
    }
}
