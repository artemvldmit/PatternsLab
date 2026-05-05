package com.example.shop.patterns.chain;

import com.example.shop.patterns.order.Order;

public class CouponValidator extends Validator {
    @Override
    protected boolean check(Order order) {
        System.out.println("CouponValidator: OK");
        return true;
    }
}
