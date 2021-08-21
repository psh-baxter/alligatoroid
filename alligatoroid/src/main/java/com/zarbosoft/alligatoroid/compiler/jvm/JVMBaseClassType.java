package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMDescriptor;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMSharedClass;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarHalfType;
import com.zarbosoft.rendaw.common.TSMap;

public class JVMBaseClassType {
  public final String jvmInternalClass;
  public TSMap<Object, JVMType> fields;

  public JVMBaseClassType(String jvmExternalClass) {
    this.jvmInternalClass = JVMDescriptor.internalName(jvmExternalClass);
  }
}
