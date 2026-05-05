package com.example.shop.patterns.state.states;

import com.example.shop.patterns.state.OrderContext;
import com.example.shop.patterns.state.OrderState;

public class ShippedState implements OrderState {
    @Override
    public void next(OrderContext ctx) { ctx.setState(new DeliveredState()); }

    @Override
    public String getName() { return "SHIPPED"; }
}
