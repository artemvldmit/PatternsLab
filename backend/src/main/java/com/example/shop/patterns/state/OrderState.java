package com.example.shop.patterns.state;

public interface OrderState {
    void next(OrderContext ctx);
    String getName();
}
