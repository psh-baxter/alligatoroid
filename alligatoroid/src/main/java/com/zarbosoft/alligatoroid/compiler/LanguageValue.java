package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.alligatoroid.compiler.mortar.MortarValue;

public abstract class LanguageValue implements MortarValue {
  public final Location location;

  public LanguageValue(Location id) {
    this.location = id;
  }

  @Override
  public abstract EvaluateResult evaluate(Context context);

  @Override
  public Location location() {
    return location;
  }
}
