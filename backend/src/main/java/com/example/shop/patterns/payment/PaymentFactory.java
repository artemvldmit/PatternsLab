package com.example.shop.patterns.payment;

/**
 * Abstract Factory: creates a family of related payment objects
 * (processor + validator) for a specific payment method.
 * Each concrete factory ensures processor and validator are compatible.
 */
public interface PaymentFactory {
    PaymentProcessor createProcessor();
    PaymentValidator createValidator();
}
