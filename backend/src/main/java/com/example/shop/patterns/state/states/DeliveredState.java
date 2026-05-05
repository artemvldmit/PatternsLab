package com.example.shop.patterns.state.states;

import com.example.shop.patterns.state.OrderContext;
import com.example.shop.patterns.state.OrderState;

public class DeliveredState implements OrderState {
    @Override
    public void next(OrderContext ctx) { /* terminal */ }

    @Override
    public String getName() { return "DELIVERED"; }
}
