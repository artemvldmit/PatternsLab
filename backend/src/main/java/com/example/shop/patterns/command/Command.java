package com.example.shop.patterns.command;

/**
 * Command interface for cart/admin actions (supporting undo/redo).
 */
public interface Command {
    void execute();
    void undo();
}
