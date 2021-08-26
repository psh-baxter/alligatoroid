package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.alligatoroid.compiler.mortar.SimpleValue;
import com.zarbosoft.rendaw.common.ROList;

public abstract class LanguageValue implements SimpleValue {
  public final Location location;
  public final boolean hasLowerInSubtree;

  public LanguageValue(Location id, boolean hasLowerInSubtree) {
    this.location = id;
    this.hasLowerInSubtree = hasLowerInSubtree;
  }

  protected static boolean hasLowerInSubtree(ROList<Value> values) {
    boolean out = false;
    for (Value value : values) {
      if (value instanceof LanguageValue) out = out || ((LanguageValue) value).hasLowerInSubtree;
    }
    return out;
  }

  protected static boolean hasLowerInSubtree(Value... values) {
    boolean out = false;
    for (Value value : values) {
      if (value instanceof LanguageValue) out = out || ((LanguageValue) value).hasLowerInSubtree;
    }
    return out;
  }

  @Override
  public abstract EvaluateResult evaluate(Context context);

  @Override
  public Location location() {
    return location;
  }
}
