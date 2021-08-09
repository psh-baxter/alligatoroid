package com.zarbosoft.alligatoroid.compiler;

public abstract class OkValue implements Value {
  @Override
  public Value call(Context context, Location location, Value argument) {
    context.errors.add(Error.callNotSupported(location));
    return ErrorValue.error;
  }

  @Override
  public Value access(Context context, Location location, Value field) {
    context.errors.add(Error.accessNotSupported(location));
    return ErrorValue.error;
  }

  @Override
  public Value mergePrevious(Context context, Value previous) {
    return null;
  }

  @Override
  public Value mergeNext(Context context, Value next) {
    return this;
  }
}
