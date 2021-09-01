package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.TSList;

class StateArray extends BaseState {
  private final StatePrototype inner;
  TSList<State> elements = new TSList<>();

  public StateArray(StatePrototype inner) {
    this.inner = inner;
  }

  @Override
  public void eatType(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath, String name) {
    push(errors, stack, luxemPath);
    stack.last().eatType(errors, stack, luxemPath, name);
  }

  @Override
  public void eatPrimitive(
      TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath, String value) {
    push(errors, stack, luxemPath);
    stack.last().eatPrimitive(errors, stack, luxemPath, value);
  }

  @Override
  public void eatArrayBegin(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath) {
    push(errors, stack, luxemPath);
    stack.last().eatArrayBegin(errors, stack, luxemPath);
  }

  private void push(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath) {
    State out = inner.create(errors, luxemPath, stack);
    stack.add(out);
  }

  @Override
  public void eatArrayEnd(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath) {
    stack.removeLast();
  }

  @Override
  public void eatRecordBegin(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath) {
    push(errors, stack, luxemPath);
    stack.last().eatRecordBegin(errors, stack, luxemPath);
  }

  @Override
  public Object build(TSList<Error> errors) {
    if (!ok) return null;
    TSList<Object> out = new TSList<>();
    for (State element : elements) {
      Object value = element.build(errors);
      if (value == null) return null;
      out.add(value);
    }
    return out;
  }
}
