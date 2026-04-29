package com.example.shop.patterns.facade;

import com.example.shop.patterns.order.Order;
import com.example.shop.patterns.payment.PaymentFactory;
import com.example.shop.patterns.payment.PaymentProcessor;
import com.example.shop.patterns.delivery.DeliveryStrategy;

import java.util.Map;

/**
 * Facade to simplify order placement: validate, charge, schedule delivery.
 */
public class OrderFacade {
    private final PaymentFactory paymentFactory;
    private final DeliveryStrategy deliveryStrategy;

    public OrderFacade(PaymentFactory paymentFactory, DeliveryStrategy deliveryStrategy) {
        this.paymentFactory = paymentFactory;
        this.deliveryStrategy = deliveryStrategy;
    }

    public boolean placeOrder(Order order, Map<String,String> paymentDetails) {
        PaymentProcessor p = paymentFactory.createProcessor();
        double total = order.getItems().stream().mapToDouble(i -> i.getPrice()).sum();
        boolean ok = p.process(total, "USD", paymentDetails);
        if (!ok) return false;
        double delivery = deliveryStrategy.calculate(order);
        System.out.println("Delivery method: " + deliveryStrategy.getName() + ", cost=" + delivery);
        return true;
    }
}
