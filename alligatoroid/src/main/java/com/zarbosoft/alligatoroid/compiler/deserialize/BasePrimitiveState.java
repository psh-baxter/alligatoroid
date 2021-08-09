package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.TSList;

abstract class BasePrimitiveState implements State {
  boolean ok = true;

  void errorNonPrimitive(Context context, TSList<State> stack, LuxemPath luxemPath) {
    context.errors.add(Error.deserializeNotPrimitive(luxemPath));
    stack.add(StateErrorNonPrimitive.state);
    ok = false;
  }

  @Override
  public void eatArrayBegin(Context context, TSList<State> stack, LuxemPath luxemPath) {
    errorNonPrimitive(context, stack, luxemPath);
  }

  @Override
  public void eatArrayEnd(Context context, TSList<State> stack, LuxemPath luxemPath) {
    throw new Assertion();
  }

  @Override
  public void eatRecordBegin(Context context, TSList<State> stack, LuxemPath luxemPath) {
    errorNonPrimitive(context, stack, luxemPath);
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
  public void eatType(Context context, TSList<State> stack, LuxemPath luxemPath, String name) {
    context.errors.add(Error.deserializeNotTyped(luxemPath));
    ok = false;
  }
}
