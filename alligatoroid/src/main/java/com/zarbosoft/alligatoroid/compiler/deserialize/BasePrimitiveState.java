package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.ModuleContext;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.TSList;

abstract class BasePrimitiveState implements State {
  boolean ok = true;

  void errorNonPrimitive(ModuleContext context, TSList<State> stack, LuxemPath luxemPath) {
    context.errors.add(Error.deserializeNotPrimitive(luxemPath));
    stack.add(StateErrorNonPrimitive.state);
    ok = false;
  }

  @Override
  public void eatArrayBegin(ModuleContext context, TSList<State> stack, LuxemPath luxemPath) {
    errorNonPrimitive(context, stack, luxemPath);
  }

  @Override
  public void eatArrayEnd(ModuleContext context, TSList<State> stack, LuxemPath luxemPath) {
    throw new Assertion();
  }

  @Override
  public void eatRecordBegin(ModuleContext context, TSList<State> stack, LuxemPath luxemPath) {
    errorNonPrimitive(context, stack, luxemPath);
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
  public void eatType(ModuleContext context, TSList<State> stack, LuxemPath luxemPath, String name) {
    context.errors.add(Error.deserializeNotTyped(luxemPath));
    ok = false;
  }
}
