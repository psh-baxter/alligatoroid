package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.TSList;

public class StatePrototypeArray implements StatePrototype {
  private final StatePrototype inner;

  public StatePrototypeArray(StatePrototype inner) {
    this.inner = inner;
  }

  @Override
  public State create(TSList<Error> errors, LuxemPath luxemPath, TSList<State> stack) {
    BaseState out = new StateArray(inner);
    stack.addVar(out, StateArrayBegin.state);
    return out;
  }
}
