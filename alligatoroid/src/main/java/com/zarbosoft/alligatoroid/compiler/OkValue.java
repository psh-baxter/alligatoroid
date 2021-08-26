package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.rendaw.common.ROPair;

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
  public default ROPair<EvaluateResult, Binding> bind(Context context, Location location) {
    context.module.errors.add(Error.bindNotSupported(location));
    return new ROPair<>(EvaluateResult.error, null);
  }

  @Override
  default Location location() {
    return null;
  }
}
