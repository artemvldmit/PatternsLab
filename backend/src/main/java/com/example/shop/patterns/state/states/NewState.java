package com.example.shop.patterns.state.states;

import com.example.shop.patterns.state.OrderContext;
import com.example.shop.patterns.state.OrderState;

public class NewState implements OrderState {
    @Override
    public void next(OrderContext ctx) { ctx.setState(new PaidState()); }

    @Override
    public String getName() { return "NEW"; }
}
