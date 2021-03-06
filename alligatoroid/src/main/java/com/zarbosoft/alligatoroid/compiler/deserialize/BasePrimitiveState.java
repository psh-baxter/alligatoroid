package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.TSList;

abstract class BasePrimitiveState implements State {
  boolean ok = true;

  void errorNonPrimitive(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath) {
    errors.add(Error.deserializeNotPrimitive(luxemPath));
    stack.add(StateErrorMultiple.state);
    ok = false;
  }

  @Override
  public void eatArrayBegin(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath) {
    errorNonPrimitive(errors, stack, luxemPath);
  }

  @Override
  public void eatArrayEnd(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath) {
    throw new Assertion();
  }

  @Override
  public void eatRecordBegin(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath) {
    errorNonPrimitive(errors, stack, luxemPath);
  }

  @Override
  public void eatRecordEnd(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath) {
    throw new Assertion();
  }

  @Override
  public void eatKey(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath, String name) {
    throw new Assertion();
  }

  @Override
  public void eatType(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath, String name) {
    errors.add(Error.deserializeNotTyped(luxemPath));
    ok = false;
  }
}
