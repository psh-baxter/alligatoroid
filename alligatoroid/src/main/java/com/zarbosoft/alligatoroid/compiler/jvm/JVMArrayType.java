package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMDescriptor;

public class JVMArrayType extends JVMObjectType {
  public final JVMDataType elementType;

  public JVMArrayType(JVMDataType elementType) {
    this.elementType = elementType;
  }

  @Override
  public String jvmDesc() {
    return JVMDescriptor.arrayDescriptor(elementType.jvmDesc());
  }
}
