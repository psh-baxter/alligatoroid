package com.zarbosoft.pidgoon;

import java.util.Map;

/**
 * This contains user-accessible state for the parse. This is immutable - any modifications return a
 * new Store. This is because parses can branch.
 *
 * <p>Arguments to the parse can be provided as an "env" map.
 */
public abstract class Store {
  public Object color;

  public Store(final Object color) {
    this.color = color;
  }

  public abstract <Y> Y split();

  /**
   * Called when the parse exits a branch/leaf in the syntax tree.
   *
   * @return
   */
  public Store pop() {
    return this;
  }

  /**
   * Called when the parse descends into a branch/leaf in the syntax tree.
   *
   * @return
   */
  public Store push() {
    return this;
  }

  /**
   * Called when the parse synthetically descends `size` levels in the syntax tree,
   *
   * @param size
   * @return
   */
  public Store inject(final long size) {
    return this;
  }

  /**
   * This branch has a result
   *
   * @return
   */
  public abstract boolean hasResult();

  /**
   * Returns the result. Behavior undefined if no result.
   *
   * @return
   */
  public abstract Object result();

  /**
   * Set the current position
   *
   * @param position
   * @return
   */
  public abstract Store record(Position position);
}
