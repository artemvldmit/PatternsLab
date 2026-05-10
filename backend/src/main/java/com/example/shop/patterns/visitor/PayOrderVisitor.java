package com.example.shop.patterns.visitor;

import com.example.shop.patterns.order.Order;

public class PayOrderVisitor implements OrderVisitor {
    @Override
    public void visit(Order order) {
        if (!"NEW".equals(order.getStatus())) return;
        order.setStatus("PAID");
    }

    @Override
    public String getName() { return "PAY"; }
}
