package com.example.shop.patterns.chain;

import com.example.shop.patterns.order.Order;

public class StockValidator extends Validator {
    @Override
    protected boolean check(Order order) {
        // Demo: always true
        System.out.println("StockValidator: OK");
        return true;
    }
}
