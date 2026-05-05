package com.example.shop.patterns.payment;

import java.util.Map;

/**
 * Concrete factory for PayPal payments.
 */
public class PayPalFactory implements PaymentFactory {
    @Override
    public PaymentProcessor createProcessor() {
        return new PayPalAdapter();
    }

    @Override
    public PaymentValidator createValidator() {
        return new PayPalValidator();
    }

    // Simple adapter simulating external PayPal API
    static class PayPalAdapter implements PaymentProcessor {
        @Override
        public boolean process(double amount, String currency, Map<String, String> details) {
            System.out.println("[PayPal] Processing " + amount + " " + currency);
            return true;
        }
    }
}
