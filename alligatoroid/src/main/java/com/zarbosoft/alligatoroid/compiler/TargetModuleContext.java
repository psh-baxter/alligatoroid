package com.zarbosoft.alligatoroid.compiler;

public interface TargetModuleContext {
  public TargetCode merge(Context context, Location location, Iterable<TargetCode> values);
}
