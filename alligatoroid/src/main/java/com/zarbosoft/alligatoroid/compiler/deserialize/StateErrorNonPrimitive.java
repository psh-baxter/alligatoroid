package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.ErrorValue;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.TSList;

public class StateErrorNonPrimitive implements State {
  public static final StateErrorNonPrimitive state = new StateErrorNonPrimitive();
  @Override
  public void eatArrayBegin(Context context, TSList<State> stack, LuxemPath luxemPath) {
    stack.add(this);
  }

  @Override
  public void eatArrayEnd(Context context, TSList<State> stack, LuxemPath luxemPath) {
    stack.removeLast();
  }

  @Override
  public void eatRecordBegin(Context context, TSList<State> stack, LuxemPath luxemPath) {
    stack.add(this);
  }

  @Override
  public void eatRecordEnd(Context context, TSList<State> stack, LuxemPath luxemPath) {
    stack.removeLast();
  }

  @Override
  public void eatKey(Context context, TSList<State> stack, LuxemPath luxemPath, String name) {}

  @Override
  public void eatType(Context context, TSList<State> stack, LuxemPath luxemPath, String name) {}

  @Override
  public void eatPrimitive(Context context, TSList<State> stack, LuxemPath luxemPath, String value) {}

  @Override
  public Object build(Context context) {
    return ErrorValue.error;
  }
}
