package com.example.shop.patterns.memento;

/**
 * Memento storing snapshot data (JSON string) of cart.
 */
public class CartMemento {
    private final String data;
    public CartMemento(String data) { this.data = data; }
    public String getData() { return data; }
}
