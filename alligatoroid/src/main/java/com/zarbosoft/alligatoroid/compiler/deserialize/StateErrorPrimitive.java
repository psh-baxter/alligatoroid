package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.ErrorValue;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.TSList;

public class StateErrorPrimitive implements State {
  public static StateErrorPrimitive state = new StateErrorPrimitive();

  @Override
  public void eatArrayBegin(Context context, TSList<State> stack, LuxemPath luxemPath) {
    stack.removeLast();
    stack.add(StateErrorNonPrimitive.state);
  }

  @Override
  public void eatArrayEnd(Context context, TSList<State> stack, LuxemPath luxemPath) {
    throw new Assertion();
  }

  @Override
  public void eatRecordBegin(Context context, TSList<State> stack, LuxemPath luxemPath) {
    stack.removeLast();
    stack.add(StateErrorNonPrimitive.state);
  }

  @Override
  public void eatRecordEnd(Context context, TSList<State> stack, LuxemPath luxemPath) {
    throw new Assertion();
  }

  @Override
  public void eatKey(Context context, TSList<State> stack, LuxemPath luxemPath, String name) {
    throw new Assertion();
  }

  @Override
  public void eatType(Context context, TSList<State> stack, LuxemPath luxemPath, String name) {}

  @Override
  public void eatPrimitive(Context context, TSList<State> stack, LuxemPath luxemPath, String value) {
    stack.removeLast();
  }

  @Override
  public Object build(Context context) {
    return ErrorValue.error;
  }
}
