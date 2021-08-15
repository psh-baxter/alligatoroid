package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.ModuleContext;
import com.zarbosoft.alligatoroid.compiler.ErrorValue;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.TSList;

public class StateErrorNonPrimitive implements State {
  public static final StateErrorNonPrimitive state = new StateErrorNonPrimitive();
  @Override
  public void eatArrayBegin(ModuleContext context, TSList<State> stack, LuxemPath luxemPath) {
    stack.add(this);
  }

  @Override
  public void eatArrayEnd(ModuleContext context, TSList<State> stack, LuxemPath luxemPath) {
    stack.removeLast();
  }

  @Override
  public void eatRecordBegin(ModuleContext context, TSList<State> stack, LuxemPath luxemPath) {
    stack.add(this);
  }

  @Override
  public void eatRecordEnd(ModuleContext context, TSList<State> stack, LuxemPath luxemPath) {
    stack.removeLast();
  }

  @Override
  public void eatKey(ModuleContext context, TSList<State> stack, LuxemPath luxemPath, String name) {}

  @Override
  public void eatType(ModuleContext context, TSList<State> stack, LuxemPath luxemPath, String name) {}

  @Override
  public void eatPrimitive(ModuleContext context, TSList<State> stack, LuxemPath luxemPath, String value) {}

  @Override
  public Object build(ModuleContext context) {
    return ErrorValue.error;
  }
}
