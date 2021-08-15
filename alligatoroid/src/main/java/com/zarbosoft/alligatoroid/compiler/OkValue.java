package com.zarbosoft.alligatoroid.compiler;

public interface OkValue extends Value {
  @Override
  public default EvaluateResult call(Context context, Location location, Value argument) {
    context.module.errors.add(Error.callNotSupported(location));
    return EvaluateResult.error;
  }

  @Override
  public default EvaluateResult access(Context context, Location location, Value field) {
    context.module.errors.add(Error.accessNotSupported(location));
    return EvaluateResult.error;
  }

  @Override
  default Location location() {
    return null;
  }
}
