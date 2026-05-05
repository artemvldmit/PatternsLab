package com.example.shop.patterns.template;

import java.util.Map;

/**
 * Template Method pattern for payment processing flow.
 */
public abstract class PaymentTemplate {
    public final boolean process(double amount, String currency, Map<String,String> details) {
        if (!validate(details)) return false;
        if (!authorize(details)) return false;
        if (!capture(amount, currency, details)) return false;
        log(amount, currency, details);
        return true;
    }

    protected abstract boolean validate(Map<String,String> details);
    protected abstract boolean authorize(Map<String,String> details);
    protected abstract boolean capture(double amount, String currency, Map<String,String> details);
    protected void log(double amount, String currency, Map<String,String> details) {
        System.out.println("Payment processed: " + amount + " " + currency);
    }
}
