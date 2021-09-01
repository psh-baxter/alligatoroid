package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.TSList;

public class StateArrayBegin extends BaseState {
  public static final StateArrayBegin state = new StateArrayBegin();

  private StateArrayBegin() {}

  @Override
  public void eatArrayBegin(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath) {
    stack.removeLast();
  }
}
