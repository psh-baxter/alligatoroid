package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.luxem.read.path.LuxemPath;

public abstract class BaseStatePrototype implements StatePrototype {
  @Override
  public State createArray(Context context, LuxemPath luxemPath, String type) {
    context.errors.add(Error.deserializeNotArray(luxemPath));
    return StateErrorNonPrimitive.state;
  }

  @Override
  public State createRecord(Context context, LuxemPath luxemPath, String type) {
    context.errors.add(Error.deserializeNotRecord(luxemPath));
    return StateErrorNonPrimitive.state;
  }

  @Override
  public State createPrimitive(Context context, LuxemPath luxemPath, String type) {
    context.errors.add(Error.deserializeNotPrimitive(luxemPath));
    return StateErrorPrimitive.state;
  }
}
