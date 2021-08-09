package com.zarbosoft.alligatoroid.compiler;

public interface Value {
  Value call(Context context, Location location, Value argument);

  Value access(Context context, Location location, Value field);

  default Value evaluate(Context context) {
    return this;
  }

  /**
   * Merge values in a sequence. If this node doesn't know how to merge, return null and mergeNext
   * will be used instead. Return this value carrying previous side effects.
   *
   * @param context
   * @param previous
   * @return
   */
  Value mergePrevious(Context context, Value previous);

  /**
   * Merge values in a sequence. Return the next value carrying this's side effects. Only called if
   * mergePrevious fails. Use a dumb merge process (may assume no side effects).
   *
   * @param context
   * @param next
   * @return
   */
  Value mergeNext(Context context, Value next);

  Value drop(Context context);
}
