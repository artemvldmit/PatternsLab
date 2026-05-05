package com.example.shop.patterns.payment;

import java.util.Map;

public class PayPalValidator implements PaymentValidator {
    @Override
    public boolean validate(Map<String, String> details) {
        String email = details.getOrDefault("email", "");
        return email.contains("@") && email.contains(".");
    }
}
