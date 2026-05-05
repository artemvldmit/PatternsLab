package com.example.shop.patterns.command;

import com.example.shop.patterns.factory.Product;

import java.util.List;

/**
 * Mass price update command for admin (applies delta to multiple products).
 */
public class MassPriceUpdateCommand implements Command {
    private final List<Product> products;
    private final double delta;

    public MassPriceUpdateCommand(List<Product> products, double delta) {
        this.products = products;
        this.delta = delta;
    }

    @Override
    public void execute() {
        for (Product p : products) {
            // naive reflection via subclass field manipulation isn't ideal; for demo assume accessible
            // using product price via subclass not mutable; in real app use service to update DB
            System.out.println("Updating product " + p.getName() + " by " + delta);
        }
    }

    @Override
    public void undo() {
        System.out.println("Undo mass price update");
    }
}
