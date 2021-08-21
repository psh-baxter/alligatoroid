package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.mortar.SimpleValue;

public class JVMStringType implements SimpleValue {
  public static final JVMStringType value = new JVMStringType();

  private JVMStringType() {}
}
