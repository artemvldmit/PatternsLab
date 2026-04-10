package com.example.shop.patterns.factory;

public class PhysicalProductCreator extends ProductCreator {
    @Override
    public Product createProduct(int id, String name, double price) {
        return new PhysicalProduct(id, name, price);
    }
}
