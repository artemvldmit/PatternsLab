package com.example.shop.patterns.payment;

import java.util.Map;

/**
 * Strategy for processing payments. Adapter and factories will provide implementations.
 */
public interface PaymentProcessor {
    boolean process(double amount, String currency, Map<String, String> details);
}
