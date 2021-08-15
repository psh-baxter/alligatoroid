package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.ErrorValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Serializable;
import com.zarbosoft.alligatoroid.compiler.Value;

public interface WholeValue extends MortarValue, MortarLowerableValue, Serializable {
  public static WholeValue getWhole(Context context, Location location, Value value) {
    if (value == ErrorValue.error) return null;
    if (!(value instanceof WholeValue)) {
      context.module.errors.add(Error.valueNotWhole(location, value));
      return null;
    }
    return (WholeValue) value;
  }

  Object concreteValue();
}
