package com.zarbosoft.pidgoon.model;

public abstract class Leaf<E> {
  protected Leaf() {}

  /**
   * The current color of this branch, as set by a Color node
   *
   * @param <T>
   * @return
   */
  public abstract <T> T color();

  public abstract void parse(Grammar grammar, Step step, E event);
}
