package com.example.shop.patterns.factory;

import java.util.Map;

/**
 * Dispatcher that resolves the correct ProductCreator (Factory Method) by type string.
 * Clients can also use concrete creators directly for stronger typing.
 */
public class ProductFactory {
    private static final Map<String, ProductCreator> CREATORS = Map.of(
        "DIGITAL",      new DigitalProductCreator(),
        "PHYSICAL",     new PhysicalProductCreator(),
        "SUBSCRIPTION", new SubscriptionProductCreator()
    );

    public static Product create(String type, int id, String name, double price) {
        ProductCreator creator = CREATORS.get(type.toUpperCase());
        if (creator == null) throw new IllegalArgumentException("Unknown product type: " + type);
        return creator.createProduct(id, name, price);
    }
}
