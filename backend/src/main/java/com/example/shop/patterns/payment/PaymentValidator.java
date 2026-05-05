package com.example.shop.patterns.payment;

import java.util.Map;

/**
 * Second product in the payment family (part of Abstract Factory).
 * Each concrete factory provides a validator matching its payment method.
 */
public interface PaymentValidator {
    boolean validate(Map<String, String> details);
}
