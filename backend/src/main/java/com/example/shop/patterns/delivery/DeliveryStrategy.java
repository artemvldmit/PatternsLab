package com.example.shop.patterns.delivery;

import com.example.shop.patterns.order.Order;

/**
 * Strategy pattern for delivery calculation/processing.
 */
public interface DeliveryStrategy {
    double calculate(Order order);
    String getName();
}
