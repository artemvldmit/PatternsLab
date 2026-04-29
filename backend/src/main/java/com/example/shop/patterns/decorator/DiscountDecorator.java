package com.example.shop.patterns.decorator;

import com.example.shop.patterns.factory.Product;

public class DiscountDecorator extends ProductDecorator {
    private final double percent;

    public DiscountDecorator(Product delegate, double percent) {
        super(delegate);
        this.percent = percent;
    }

    @Override
    public double getPrice() { return delegate.getPrice() * (1.0 - percent/100.0); }
}
