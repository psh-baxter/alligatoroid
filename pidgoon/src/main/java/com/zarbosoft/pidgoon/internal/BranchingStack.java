package com.zarbosoft.pidgoon.internal;

public class BranchingStack<T> {
  private final T top;
  private final BranchingStack<T> parent;

  public BranchingStack(T t) {
    top = t;
    parent = null;
  }

  private BranchingStack(BranchingStack<T> parent, T t) {
    top = t;
    this.parent = parent;
  }

  public T top() {
    return top;
  }

  public BranchingStack<T> push(T t) {
    return new BranchingStack<>(this, t);
  }

  public BranchingStack<T> pop() {
    return parent;
  }

  public BranchingStack<T> set(T t) {
    return new BranchingStack<>(parent, t);
  }

  private long size(long start) {
    if (parent == null) return start + 1;
    return parent.size(start + 1);
  }

  public long size() {
    return size(0);
  }

  public boolean isLast() {
    return parent == null;
  }
}
