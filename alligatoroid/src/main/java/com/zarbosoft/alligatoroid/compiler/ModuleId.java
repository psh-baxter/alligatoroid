package com.zarbosoft.alligatoroid.compiler;

public interface ModuleId extends TreeSerializable {
  String hash();

  boolean equal1(ModuleId other);

  <T> T dispatch(Dispatcher<T> dispatcher);

  interface Dispatcher<T> {
    T handle(LocalModuleId id);
  }
}
