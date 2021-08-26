package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.Value;

public interface JVMType {
  public Value asValue(JVMProtocode code);
}
