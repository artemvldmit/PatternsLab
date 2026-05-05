package com.example.shop.patterns.state;

public class OrderContext {
    private OrderState state;
    public OrderContext(OrderState s) { this.state = s; }
    public void setState(OrderState s) { this.state = s; }
    public OrderState getState() { return state; }
}
