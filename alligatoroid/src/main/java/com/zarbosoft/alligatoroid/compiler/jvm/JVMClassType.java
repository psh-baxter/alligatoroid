package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMSharedClass;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSSet;

public class JVMClassType extends JVMBaseClassType {
  public final TSSet<String> incompleteMethods = new TSSet<>();
  public final JVMSharedClass jvmClass;
  private byte[] built;

  public JVMClassType(String jvmExternalClass) {
    super(jvmExternalClass);
    this.jvmClass = new JVMSharedClass(jvmExternalClass);
  }

  public JVMMethod defineMethod(String name) {
    incompleteMethods.add(name);
    return new JVMMethod(this, name);
  }

  private void build(TSList<Error> errors) {
    if (built != null) return;
    if (incompleteMethods.some()) {
      errors.add(Error.methodsNotDefined(incompleteMethods));
      return;
    }
    built = jvmClass.render();
  }

  public byte[] bytes() {
    TSList<Error> errors = new TSList<>();
    build(errors);
    if (errors.some()) throw new MultiError(errors);
    return built;
  }
}
