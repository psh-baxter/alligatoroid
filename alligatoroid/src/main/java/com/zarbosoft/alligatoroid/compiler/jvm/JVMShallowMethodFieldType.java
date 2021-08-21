package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMDescriptor;
import com.zarbosoft.alligatoroid.compiler.mortar.Record;
import com.zarbosoft.rendaw.common.Assertion;

public class JVMShallowMethodFieldType implements JVMType {
  public final JVMBaseClassType base;
  public final JVMType returnType;
  public final String name;
  public final String jvmDesc;

  public JVMShallowMethodFieldType(
      JVMBaseClassType base, JVMType returnType, String name, String jvmDesc) {
    this.base = base;
    this.returnType = returnType;
    this.name = name;
    this.jvmDesc = jvmDesc;
  }

  public static MethodSpecDetails specDetails(Record spec) {
    JVMType returnType = (JVMType) spec.data.get("out");
    String jvmSigDesc;
    if (spec.data.get("in") == JVMStringType.value) {
      jvmSigDesc = JVMDescriptor.objReal(String.class);
    } else throw new Assertion();
    return new MethodSpecDetails(returnType, jvmSigDesc);
  }

  @Override
  public Value stackAsValue(JVMCode code) {
    throw new Assertion();
  }

  @Override
  public Value asValue(JVMProtocode code) {
    return new JVMMethodField(code, this);
  }

  public static class MethodSpecDetails {
    public final JVMType returnType;
    public final String jvmSigDesc;

    public MethodSpecDetails(JVMType returnType, String jvmSigDesc) {
      this.returnType = returnType;
      this.jvmSigDesc = jvmSigDesc;
    }
  }
}
