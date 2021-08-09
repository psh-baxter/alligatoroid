package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Context;
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
  public void eatArrayBegin(Context context, TSList<State> stack, LuxemPath luxemPath) {
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
  public void eatArrayEnd(Context context, TSList<State> stack, LuxemPath luxemPath) {
    stack.removeLast();
  }

  @Override
  public void eatRecordBegin(Context context, TSList<State> stack, LuxemPath luxemPath) {
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
  public void eatRecordEnd(Context context, TSList<State> stack, LuxemPath luxemPath) {
    stack.removeLast();
  }

  @Override
  public void eatKey(Context context, TSList<State> stack, LuxemPath luxemPath, String name) {
    key = name;
  }

  @Override
  public void eatType(Context context, TSList<State> stack, LuxemPath luxemPath, String name) {
    type = name;
  }

  @Override
  public void eatPrimitive(
      Context context, TSList<State> stack, LuxemPath luxemPath, String value) {
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
