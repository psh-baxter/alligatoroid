package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.TSList;

public class StateErrorMultiple implements State {
  public static final StateErrorMultiple state = new StateErrorMultiple();

  @Override
  public void eatArrayBegin(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath) {
    stack.add(this);
  }

  @Override
  public void eatArrayEnd(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath) {
    stack.removeLast();
  }

  @Override
  public void eatRecordBegin(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath) {
    stack.add(this);
  }

  @Override
  public void eatRecordEnd(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath) {
    stack.removeLast();
  }

  @Override
  public void eatKey(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath, String name) {}

  @Override
  public void eatType(
      TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath, String name) {}

  @Override
  public void eatPrimitive(
      TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath, String value) {}

  @Override
  public Object build(TSList<Error> errors) {
    return null;
  }
}
