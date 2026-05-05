package com.example.shop.patterns.payment;

import com.example.shop.patterns.template.PaymentTemplate;

import java.util.Map;

/**
 * Concrete factory for Credit Card payments.
 */
public class CreditCardFactory implements PaymentFactory {
    @Override
    public PaymentProcessor createProcessor() {
        return new CreditCardProcessor();
    }

    @Override
    public PaymentValidator createValidator() {
        return new CreditCardValidator();
    }

    /**
     * Extends PaymentTemplate so the validate→authorize→capture→log skeleton
     * defined by the Template Method pattern is actually executed.
     */
    static class CreditCardProcessor extends PaymentTemplate implements PaymentProcessor {
        @Override
        protected boolean validate(Map<String, String> details) {
            return true;
        }

        @Override
        protected boolean authorize(Map<String, String> details) {
            return true;
        }

        @Override
        protected boolean capture(double amount, String currency, Map<String, String> details) {
            System.out.println("[CreditCard] Charging " + amount + " " + currency);
            return true;
        }
    }
}
