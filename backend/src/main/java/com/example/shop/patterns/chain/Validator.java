package com.example.shop.patterns.chain;

import com.example.shop.patterns.order.Order;

/**
 * Chain of Responsibility: validators for order processing
 */
public abstract class Validator {
    protected Validator next;
    public Validator setNext(Validator n) { this.next = n; return n; }
    public boolean validate(Order order) {
        boolean ok = check(order);
        if (!ok) return false;
        if (next != null) return next.validate(order);
        return true;
    }
    protected abstract boolean check(Order order);
}
