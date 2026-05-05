package com.example.shop.patterns.payment;

import java.util.Map;

/**
 * Adapter: adapts a hypothetical Stripe API to PaymentProcessor interface.
 */
public class StripeAdapter implements PaymentProcessor {
    // Simulated external API client
    static class StripeClient {
        public boolean makeCharge(double amount, String currency, Map<String, String> meta) {
            System.out.println("[StripeClient] charge " + amount + " " + currency);
            return true;
        }
    }

    private final StripeClient client = new StripeClient();

    @Override
    public boolean process(double amount, String currency, Map<String, String> details) {
        return client.makeCharge(amount, currency, details);
    }
}
