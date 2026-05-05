package com.example.shop.patterns.order;

import com.example.shop.patterns.factory.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Order with Builder pattern for complex construction.
 */
public class Order implements Cloneable {
    private int id;
    private int userId;
    private String status;
    private final List<Product> items = new ArrayList<>();

    private Order() {}

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getStatus() { return status; }
    public List<Product> getItems() { return new ArrayList<>(items); }

    @Override
    public Order clone() {
        try {
            Order o = (Order) super.clone();
            // shallow clone items (prototype on product supports clone)
            o.items.clear();
            for (Product p : this.items) o.items.add(p.clone());
            return o;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Builder {
        private final Order order = new Order();

        public Builder id(int id) { order.id = id; return this; }
        public Builder userId(int u) { order.userId = u; return this; }
        public Builder status(String s) { order.status = s; return this; }
        public Builder addItem(Product p) { order.items.add(p); return this; }
        public Order build() { return order; }
    }
}
