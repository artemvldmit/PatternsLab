package com.example.shop.patterns.delivery;

import com.example.shop.patterns.order.Order;

public class PickupDelivery implements DeliveryStrategy {
    @Override
    public double calculate(Order order) { return 0.0; }

    @Override
    public String getName() { return "Pickup"; }
}
