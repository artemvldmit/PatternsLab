package com.example.shop.patterns.decorator;

import com.example.shop.patterns.factory.Product;

public abstract class ProductDecorator extends Product {
    protected final Product delegate;

    public ProductDecorator(Product delegate) {
        super(delegate.getId(), delegate.getName(), delegate.getPrice());
        this.delegate = delegate;
    }

    @Override
    public String getType() {
        return delegate.getType();
    }

    @Override
    public Product clone() {
        try {
            ProductDecorator copy = (ProductDecorator) super.clone();
            // delegate is final — shallow copy is acceptable for read-only use
            return copy;
        } catch (ClassCastException e) {
            throw new RuntimeException(e);
        }
    }
}
