package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMDescriptor;
import com.zarbosoft.alligatoroid.compiler.mortar.SimpleValue;

public class JVMStringType extends JVMObjectType implements SimpleValue {
  public static final JVMStringType value = new JVMStringType();

  private JVMStringType() {}

  @Override
  public String jvmDesc() {
    return JVMDescriptor.stringDescriptor;
  }
}
