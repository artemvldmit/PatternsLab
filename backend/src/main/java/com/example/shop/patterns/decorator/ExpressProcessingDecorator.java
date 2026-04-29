package com.example.shop.patterns.decorator;

import com.example.shop.patterns.factory.Product;

public class ExpressProcessingDecorator extends ProductDecorator {
    public ExpressProcessingDecorator(Product delegate) { super(delegate); }

    @Override
    public double getPrice() { return delegate.getPrice() + 10.0; }
}
