package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.mortar.Record;

public class JVMExternClassType extends JVMClassType {
  public JVMExternClassType(String jvmExternalClass) {
    super(jvmExternalClass);
  }

  public void defineFunction(String name, Record spec) {
    JVMShallowMethodFieldType.MethodSpecDetails specDetails =
        JVMShallowMethodFieldType.specDetails(spec);
    fields.putNew(
        name,
        new JVMShallowMethodFieldType(this, specDetails.returnType, name, specDetails.jvmSigDesc));
  }
}
