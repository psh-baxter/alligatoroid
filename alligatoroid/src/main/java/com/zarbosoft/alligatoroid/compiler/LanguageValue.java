package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.alligatoroid.compiler.mortar.MortarValue;

public abstract class LanguageValue extends MortarValue {
  public final Location location;

  public LanguageValue(Location id) {
    this.location = id;
  }

  @Override
  public abstract Value evaluate(Context context);

  @Override
  public Value mergeNext(Context context, Value next) {
    return next;
  }

  @Override
  public Value mergePrevious(Context context, Value previous) {
    return this;
  }
}
