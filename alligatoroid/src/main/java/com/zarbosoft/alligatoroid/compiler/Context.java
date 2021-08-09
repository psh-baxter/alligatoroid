package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.rendaw.common.TSList;

public class Context {
  public final GlobalContext globalContext;
  public final TSList<Error> errors = new TSList<>();

  public Context(GlobalContext globalContext) {
    this.globalContext = globalContext;
  }
}
