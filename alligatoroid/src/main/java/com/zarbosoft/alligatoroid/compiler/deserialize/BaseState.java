package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.TSList;

public class BaseState implements State {
  protected boolean ok = true;

  @Override
  public void eatArrayBegin(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath) {
    if (ok) {
      errors.errors.add(Error.deserializeNotArray(luxemPath));
    } else {
      ok = true;
    }
    stack.add(StateErrorMultiple.state);
  }

  @Override
  public void eatArrayEnd(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath) {
    throw new Assertion();
  }

  @Override
  public void eatRecordBegin(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath) {
    if (ok) {
      errors.errors.add(Error.deserializeNotRecord(luxemPath));
    } else {
      ok = true;
    }
    stack.add(StateErrorMultiple.state);
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
    errors.errors.add(Error.deserializeNotTyped(luxemPath));
  }

  @Override
  public void eatPrimitive(
      TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath, String value) {
    if (ok) {
      errors.errors.add(Error.deserializeNotPrimitive(luxemPath));
    } else {
      ok = true;
    }
  }

  @Override
  public Object build(TSList<Error> errors) {
    throw new Assertion();
  }
}
