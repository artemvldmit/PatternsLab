package com.example.shop.patterns.factory;

public class DigitalProductCreator extends ProductCreator {
    @Override
    public Product createProduct(int id, String name, double price) {
        return new DigitalProduct(id, name, price);
    }
}
