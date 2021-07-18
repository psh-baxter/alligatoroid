package com.zarbosoft.pidgoon.model;

public class SerialStep<R> extends Step<R>{
    public int nextLeaf;

    public Leaf<R> nextLeaf() {
        if (nextLeaf >= leaves.size()) return null;
        return leaves.get(nextLeaf++);
    }
}
