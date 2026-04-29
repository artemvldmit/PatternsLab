package com.example.shop.patterns.decorator;

import com.example.shop.patterns.factory.Product;

public class GiftWrapDecorator extends ProductDecorator {
    public GiftWrapDecorator(Product delegate) { super(delegate); }

    @Override
    public double getPrice() { return delegate.getPrice() + 5.0; }
}
