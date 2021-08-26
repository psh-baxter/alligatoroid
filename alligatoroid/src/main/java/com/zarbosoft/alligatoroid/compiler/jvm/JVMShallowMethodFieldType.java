package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMDescriptor;
import com.zarbosoft.alligatoroid.compiler.mortar.NullValue;
import com.zarbosoft.alligatoroid.compiler.mortar.Record;
import com.zarbosoft.alligatoroid.compiler.mortar.Tuple;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROList;

public class JVMShallowMethodFieldType implements JVMType {
  public final JVMBaseClassType base;
  public final JVMDataType returnType;
  public final String name;
  public final String jvmDesc;

  public JVMShallowMethodFieldType(
      JVMBaseClassType base, JVMDataType returnType, String name, String jvmDesc) {
    this.base = base;
    this.returnType = returnType;
    this.name = name;
    this.jvmDesc = jvmDesc;
  }

  public static String specDetailArg(Value value) {
    if (value instanceof JVMDataType) {
      return ((JVMDataType) value).jvmDesc();
    } else if (value == JVMStringType.value) {
      return JVMDescriptor.objDescriptorFromReal(String.class);
    } else throw new Assertion();
  }

  public static MethodSpecDetails specDetails(Record spec) {
    Object outRaw = spec.data.get("out");
    JVMDataType returnType = null;
    String returnDescriptor;
    if (outRaw == NullValue.value) {
      returnDescriptor = JVMDescriptor.voidDescriptor();
    } else {
      JVMDataType inJvmType = (JVMDataType) outRaw;
      returnDescriptor = inJvmType.jvmDesc();
      returnType = inJvmType;
    }
    Object inRaw = spec.data.get("in");
    String[] argDescriptor;
    if (inRaw instanceof Tuple) {
      ROList<Object> inTuple = ((Tuple) inRaw).data;
      argDescriptor = new String[inTuple.size()];
      for (int i = 0; i < inTuple.size(); i++) {
        argDescriptor[i] = specDetailArg((Value) inTuple.get(i));
      }
    } else {
      argDescriptor = new String[] {specDetailArg((Value) inRaw)};
    }
    return new MethodSpecDetails(returnType, JVMDescriptor.func(returnDescriptor, argDescriptor));
  }

  @Override
  public Value asValue(JVMProtocode code) {
    return new JVMMethodField(code, this);
  }

  public static class MethodSpecDetails {
    public final JVMDataType returnType;
    public final String jvmSigDesc;

    public MethodSpecDetails(JVMDataType returnType, String jvmSigDesc) {
      this.returnType = returnType;
      this.jvmSigDesc = jvmSigDesc;
    }
  }
}
