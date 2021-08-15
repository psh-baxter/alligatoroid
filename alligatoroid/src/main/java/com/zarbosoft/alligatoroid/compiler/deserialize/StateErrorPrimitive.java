package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.ModuleContext;
import com.zarbosoft.alligatoroid.compiler.ErrorValue;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.TSList;

public class StateErrorPrimitive implements State {
  public static StateErrorPrimitive state = new StateErrorPrimitive();

  @Override
  public void eatArrayBegin(ModuleContext context, TSList<State> stack, LuxemPath luxemPath) {
    stack.removeLast();
    stack.add(StateErrorNonPrimitive.state);
  }

  @Override
  public void eatArrayEnd(ModuleContext context, TSList<State> stack, LuxemPath luxemPath) {
    throw new Assertion();
  }

  @Override
  public void eatRecordBegin(ModuleContext context, TSList<State> stack, LuxemPath luxemPath) {
    stack.removeLast();
    stack.add(StateErrorNonPrimitive.state);
  }

  @Override
  public void eatRecordEnd(ModuleContext context, TSList<State> stack, LuxemPath luxemPath) {
    throw new Assertion();
  }

  @Override
  public void eatKey(ModuleContext context, TSList<State> stack, LuxemPath luxemPath, String name) {
    throw new Assertion();
  }

  @Override
  public void eatType(ModuleContext context, TSList<State> stack, LuxemPath luxemPath, String name) {}

  @Override
  public void eatPrimitive(ModuleContext context, TSList<State> stack, LuxemPath luxemPath, String value) {
    stack.removeLast();
  }

  @Override
  public Object build(ModuleContext context) {
    return ErrorValue.error;
  }
}
