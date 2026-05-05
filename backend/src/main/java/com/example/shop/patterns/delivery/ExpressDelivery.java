package com.example.shop.patterns.delivery;

import com.example.shop.patterns.order.Order;

public class ExpressDelivery implements DeliveryStrategy {
    @Override
    public double calculate(Order order) { return 25.0; }

    @Override
    public String getName() { return "Express"; }
}
