package com.example.shop.patterns.delivery;

import com.example.shop.patterns.order.Order;

public class CourierDelivery implements DeliveryStrategy {
    @Override
    public double calculate(Order order) {
        return 10.0; // flat rate demo
    }

    @Override
    public String getName() { return "Courier"; }
}
