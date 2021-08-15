package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.ModuleContext;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.luxem.read.path.LuxemPath;

public abstract class BaseStatePrototype implements StatePrototype {
  @Override
  public State createArray(ModuleContext context, LuxemPath luxemPath, String type) {
    context.errors.add(Error.deserializeNotArray(luxemPath));
    return StateErrorNonPrimitive.state;
  }

  @Override
  public State createRecord(ModuleContext context, LuxemPath luxemPath, String type) {
    context.errors.add(Error.deserializeNotRecord(luxemPath));
    return StateErrorNonPrimitive.state;
  }

  @Override
  public State createPrimitive(ModuleContext context, LuxemPath luxemPath, String type) {
    context.errors.add(Error.deserializeNotPrimitive(luxemPath));
    return StateErrorPrimitive.state;
  }
}
