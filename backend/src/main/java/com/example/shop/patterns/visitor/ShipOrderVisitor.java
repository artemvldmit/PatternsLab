package com.example.shop.patterns.visitor;

import com.example.shop.patterns.order.Order;

public class ShipOrderVisitor implements OrderVisitor {
    @Override
    public void visit(Order order) {
        if (!"PAID".equals(order.getStatus())) return;
        order.setStatus("SHIPPED");
    }

    @Override
    public String getName() { return "SHIP"; }
}
