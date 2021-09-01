package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.TSList;

public class StatePrototypeString implements StatePrototype {
  public static final StatePrototypeString instance = new StatePrototypeString();

  private StatePrototypeString() {}

  @Override
  public State create(TSList<Error> errors, LuxemPath luxemPath, TSList<State> stack) {
    return StateString.state;
  }
}
