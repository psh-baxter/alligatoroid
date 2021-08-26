package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.rendaw.common.ROPair;

public interface Value {
  EvaluateResult call(Context context, Location location, Value argument);

  EvaluateResult access(Context context, Location location, Value field);

  default EvaluateResult evaluate(Context context) {
    return new EvaluateResult(null, null, this);
  }

  TargetCode drop(Context context, Location location);

  /**
   * Creates a value to put in the scope. If error, return error value, null (add error to context).
   *
   * @param context
   * @param location
   * @return side effect, binding
   */
  ROPair<EvaluateResult, Binding> bind(Context context, Location location);

  /**
   * Location or null
   *
   * @return
   */
  Location location();
}
