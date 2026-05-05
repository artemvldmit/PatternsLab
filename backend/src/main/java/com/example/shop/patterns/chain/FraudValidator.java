package com.example.shop.patterns.chain;

import com.example.shop.patterns.order.Order;

public class FraudValidator extends Validator {
    @Override
    protected boolean check(Order order) {
        System.out.println("FraudValidator: OK");
        return true;
    }
}
