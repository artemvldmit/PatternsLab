package com.example.shop.patterns.singleton;

import com.example.shop.patterns.factory.Product;

import java.util.ArrayList;
import java.util.List;

// Singleton корзины пользователя (для демо один инстанс)
public class CartManager {
    private static CartManager instance;
    private final List<Product> items = new ArrayList<>();

    private CartManager() {}

    public static synchronized CartManager getInstance() {
        if (instance == null) instance = new CartManager();
        return instance;
    }

    public void add(Product p) { items.add(p); }
    public void remove(Product p) { items.remove(p); }
    public List<Product> getItems() { return new ArrayList<>(items); }
}
