package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.TSList;

public class StateString extends BasePrimitiveState {
  public String value;

  @Override
  public void eatPrimitive(
      TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath, String value) {
    this.value = value;
    stack.removeLast();
  }

  @Override
  public Object build(TSList<Error> errors) {
    if (!ok || value == null) return null; // was not primitive, error
    return value;
  }
}
