package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.mortar.Record;

public class JVMExternClassType extends JVMBaseClassType {
  public JVMExternClassType(String jvmExternalClass) {
    super(jvmExternalClass);
  }

  public void defineMethod(String name, Record spec) {
    // TODO take internal name as well
    JVMShallowMethodFieldType.MethodSpecDetails specDetails =
        JVMShallowMethodFieldType.specDetails(spec);
    fields.putNew(
        name,
        new JVMShallowMethodFieldType(this, specDetails.returnType, name, specDetails.jvmSigDesc));
  }
}
