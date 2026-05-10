package com.example.shop.patterns.visitor;

import com.example.shop.patterns.order.Order;

public interface OrderVisitor {
    void visit(Order order);
    String getName();
}
