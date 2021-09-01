package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.TSList;

public class StateRecordBegin extends BaseState {
  public static final StateRecordBegin state = new StateRecordBegin();

  private StateRecordBegin() {}

  @Override
  public void eatRecordBegin(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath) {
    stack.removeLast();
  }
}
