package com.example.shop.patterns.memento;

import java.util.Stack;

/**
 * Caretaker keeping snapshots for undo/redo of cart state.
 */
public class CartCaretaker {
    private final Stack<CartMemento> history = new Stack<>();

    public void save(CartMemento m) { history.push(m); }
    public CartMemento restore() { return history.isEmpty() ? null : history.pop(); }
}
