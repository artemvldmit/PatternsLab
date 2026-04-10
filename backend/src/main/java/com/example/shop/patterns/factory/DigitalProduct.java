package com.example.shop.patterns.factory;

public class DigitalProduct extends Product {
    public DigitalProduct(int id, String name, double price) {
        super(id, name, price);
    }

    @Override
    public String getType() {
        return "DIGITAL";
    }
}
