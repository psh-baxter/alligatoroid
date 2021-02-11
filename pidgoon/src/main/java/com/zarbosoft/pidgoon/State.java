package com.zarbosoft.pidgoon;

import com.zarbosoft.pidgoon.model.Parse;
import com.zarbosoft.pidgoon.model.Position;

public abstract class State {
  protected State() {}

  /**
   * The current color of this branch, as set by a Color node
   *
   * @param <T>
   * @return
   */
  public abstract <T> T color();

  public abstract void parse(Parse step, Position position);
}
