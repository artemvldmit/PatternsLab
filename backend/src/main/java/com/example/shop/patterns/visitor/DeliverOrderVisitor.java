package com.example.shop.patterns.visitor;

import com.example.shop.patterns.order.Order;

public class DeliverOrderVisitor implements OrderVisitor {
    @Override
    public void visit(Order order) {
        if (!"SHIPPED".equals(order.getStatus())) return;
        order.setStatus("DELIVERED");
    }

    @Override
    public String getName() { return "DELIVER"; }
}
