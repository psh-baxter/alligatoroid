package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.TSList;

public class StateType extends BaseState {
  public final String expected;

  public StateType(String expected) {
    this.expected = expected;
  }

  @Override
  public void eatType(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath, String name) {
    if (!expected.equals(name)) {
      errors.errors.add(Error.deserializeUnknownType(luxemPath, name, new TSList<>(expected)));
      ok = false;
    }
    stack.removeLast();
  }
}
