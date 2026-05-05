package com.example.shop.patterns.command;

import com.example.shop.patterns.factory.Product;
import com.example.shop.patterns.singleton.CartManager;

public class AddProductCommand implements Command {
    private final Product product;

    public AddProductCommand(Product product) { this.product = product; }

    @Override
    public void execute() { CartManager.getInstance().add(product); }

    @Override
    public void undo() { CartManager.getInstance().remove(product); }
}
