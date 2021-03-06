package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.TSList;

public class StateInt extends BasePrimitiveState {
  private Integer value = null;

  @Override
  public void eatPrimitive(
      TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath, String value) {
    try {
      this.value = Integer.parseInt(value);
    } catch (NumberFormatException e) {
      errors.add(Error.deserializeNotInteger(luxemPath, value));
      ok = false;
    }
    stack.removeLast();
  }

  @Override
  public Object build(TSList<Error> errors) {
    if (!ok || value == null) return null; // was not primitive, error
    return value;
  }
}
