package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.alligatoroid.compiler.mortar.SimpleValue;

public abstract class LanguageValue implements SimpleValue {
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
