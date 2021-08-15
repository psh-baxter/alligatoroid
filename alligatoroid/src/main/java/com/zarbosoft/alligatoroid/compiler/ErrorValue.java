package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.rendaw.common.ROPair;

public final class ErrorValue implements Value {
  public static final ErrorValue error = new ErrorValue();

  private ErrorValue() {}

  @Override
  public EvaluateResult call(Context context, Location location, Value argument) {
    return EvaluateResult.error;
  }

  @Override
  public EvaluateResult access(Context context, Location location, Value field) {
    return EvaluateResult.error;
  }

  @Override
  public TargetCode drop(Context context, Location location) {
    return null;
  }

  @Override
  public ROPair<EvaluateResult, Binding> bind(Context context, Location location) {
    return new ROPair<>(EvaluateResult.error, null);
  }

  @Override
  public Location location() {
    return null;
  }
}
