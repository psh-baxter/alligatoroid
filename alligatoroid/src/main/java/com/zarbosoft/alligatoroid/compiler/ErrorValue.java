package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.alligatoroid.compiler.mortar.NullValue;

public class ErrorValue implements Value {
  public static final ErrorValue error = new ErrorValue();

  @Override
  public Value call(Context context, Location location, Value argument) {
    return this;
  }

  @Override
  public Value access(Context context, Location location, Value field) {
    return this;
  }

  @Override
  public Value mergePrevious(Context context, Value previous) {
    return this;
  }

  @Override
  public Value mergeNext(Context context, Value next) {
    return next;
  }

  @Override
  public Value drop(Context context) {
    return NullValue.value;
  }
}
