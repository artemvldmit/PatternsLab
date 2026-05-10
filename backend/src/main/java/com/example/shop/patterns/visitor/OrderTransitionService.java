package com.example.shop.patterns.visitor;

import com.example.shop.patterns.order.Order;

import java.util.Map;

public class OrderTransitionService {
    private static final Map<String, OrderVisitor> TRANSITIONS = Map.of(
        "NEW",     new PayOrderVisitor(),
        "PAID",    new ShipOrderVisitor(),
        "SHIPPED", new DeliverOrderVisitor()
    );

    /** Advances the order to the next status. Returns false if already terminal. */
    public boolean advance(Order order) {
        OrderVisitor visitor = TRANSITIONS.get(order.getStatus());
        if (visitor == null) return false;
        order.accept(visitor);
        return true;
    }

    /** Applies a specific visitor by name (PAY / SHIP / DELIVER). */
    public boolean applyByName(String name, Order order) {
        return TRANSITIONS.values().stream()
            .filter(v -> v.getName().equalsIgnoreCase(name))
            .findFirst()
            .map(v -> { order.accept(v); return true; })
            .orElse(false);
    }
}
