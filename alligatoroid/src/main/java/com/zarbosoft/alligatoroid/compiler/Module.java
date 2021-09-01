package com.zarbosoft.alligatoroid.compiler;

import java.util.concurrent.CompletableFuture;

public class Module {
  public final ModuleId id;
  public final ModuleContext context;
  public final CompletableFuture<Value> result;

  public Module(ModuleId id, ModuleContext context, CompletableFuture<Value> result) {
    this.id = id;
    this.context = context;
    this.result = result;
  }
}
