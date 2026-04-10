package com.example.shop.patterns.factory;

public class PhysicalProduct extends Product {
    public PhysicalProduct(int id, String name, double price) {
        super(id, name, price);
    }

    @Override
    public String getType() {
        return "PHYSICAL";
    }
}
