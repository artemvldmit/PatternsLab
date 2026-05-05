package com.example.shop.patterns.command;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite command: groups multiple commands into one (useful for Mass operations)
 */
public class CompositeCommand implements Command {
    private final List<Command> children = new ArrayList<>();

    public void add(Command c) { children.add(c); }

    @Override
    public void execute() { children.forEach(Command::execute); }

    @Override
    public void undo() { for (int i = children.size()-1; i>=0; i--) children.get(i).undo(); }
}
