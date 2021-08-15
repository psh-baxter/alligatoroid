package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.ModuleContext;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.TSList;

public abstract class BaseNonPrimitiveState implements State {
  boolean ok = true;
  String key;
  String type;
  StatePrototype proto;

  public abstract void createdState(String key, State state);

  @Override
  public void eatArrayBegin(ModuleContext context, TSList<State> stack, LuxemPath luxemPath) {
    if (!ok) {
      stack.add(StateErrorNonPrimitive.state);
    } else if (proto == null) {
      ok = false;
      context.errors.add(Error.deserializeNotArray(luxemPath));
      stack.add(StateErrorNonPrimitive.state);
    } else {
      State next = proto.createArray(context, luxemPath, type);
      createdState(key, next);
      stack.add(next);
    }
    proto = null;
    key = null;
    type = null;
  }

  @Override
  public void eatArrayEnd(ModuleContext context, TSList<State> stack, LuxemPath luxemPath) {
    stack.removeLast();
  }

  @Override
  public void eatRecordBegin(ModuleContext context, TSList<State> stack, LuxemPath luxemPath) {
    if (!ok) {
      stack.add(StateErrorNonPrimitive.state);
    } else if (proto == null) {
      ok = false;
      context.errors.add(Error.deserializeNotRecord(luxemPath));
      stack.add(StateErrorNonPrimitive.state);
    } else {
      State next = proto.createRecord(context, luxemPath, type);
      createdState(key, next);
      stack.add(next);
    }
    proto = null;
    key = null;
    type = null;
  }

  @Override
  public void eatRecordEnd(ModuleContext context, TSList<State> stack, LuxemPath luxemPath) {
    stack.removeLast();
  }

  @Override
  public void eatKey(ModuleContext context, TSList<State> stack, LuxemPath luxemPath, String name) {
    key = name;
  }

  @Override
  public void eatType(ModuleContext context, TSList<State> stack, LuxemPath luxemPath, String name) {
    type = name;
  }

  @Override
  public void eatPrimitive(
          ModuleContext context, TSList<State> stack, LuxemPath luxemPath, String value) {
    if (!ok) {
    } else if (proto == null) {
      ok = false;
      context.errors.add(Error.deserializeNotPrimitive(luxemPath));
      stack.add(StateErrorPrimitive.state);
    } else {
      State next = proto.createPrimitive(context, luxemPath, type);
      createdState(key, next);
      stack.add(next);
      next.eatPrimitive(context, stack, luxemPath, value);
    }
    proto = null;
    key = null;
    type = null;
  }
}
