package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMSharedCode;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWSharedCode;
import com.zarbosoft.rendaw.common.ROMap;

import static org.objectweb.asm.Opcodes.POP;

public class MortarClass implements MortarHalfType, SimpleValue {
  public final String jvmInternalClass;
  public ROMap<Object, MortarHalfType> fields;

  public MortarClass(String jvmInternalClass) {
    this.jvmInternalClass = jvmInternalClass;
  }

  @Override
  public Value asValue(MortarProtocode lower) {
    return new MortarClassValue(lower, this);
  }

  @Override
  public Value stackAsValue(JVMRWSharedCode code) {
    return new MortarClassValue(
        new MortarProtocode() {
          @Override
          public JVMSharedCode lower() {
            return code;
          }

          @Override
          public TargetCode drop(Context context, Location location) {
            return new MortarCode().add(code).add(POP);
          }
        },
        this);
  }
}
