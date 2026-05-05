package com.example.shop.patterns.payment;

import java.util.Map;

public class CreditCardValidator implements PaymentValidator {
    @Override
    public boolean validate(Map<String, String> details) {
        String card = details.getOrDefault("cardNumber", "");
        return card.length() >= 13 && card.chars().allMatch(Character::isDigit);
    }
}
