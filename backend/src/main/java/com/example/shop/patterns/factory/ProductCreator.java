package com.example.shop.patterns.factory;

/**
 * Factory Method pattern — abstract Creator.
 * Subclasses override createProduct() to decide which concrete type to instantiate.
 * The template operation createPromoted() shows why Creator needs the factory method
 * without knowing the concrete class.
 */
public abstract class ProductCreator {

    // The factory method — subclasses choose the concrete Product
    public abstract Product createProduct(int id, String name, double price);

    // Template operation in Creator that USES the factory method (key GoF point)
    public Product createPromoted(int id, String name, double price, String label) {
        Product p = createProduct(id, name, price);
        System.out.println("Registering promoted [" + p.getType() + "] \"" + p.getName() + "\" " + label);
        return p;
    }
}
