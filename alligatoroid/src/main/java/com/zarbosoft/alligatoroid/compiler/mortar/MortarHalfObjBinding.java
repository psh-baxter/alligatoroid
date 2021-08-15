package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Binding;
import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMCode;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWCode;

import static org.objectweb.asm.Opcodes.ALOAD;

public class MortarHalfObjBinding implements Binding {
  public final Object key;
  public final MortarHalfType type;

  public MortarHalfObjBinding(Object key, MortarHalfType type) {
    this.key = key;
    this.type = type;
  }

  @Override
  public EvaluateResult fork(Context context, Location location) {
    return EvaluateResult.pure(
        type.asValue(
            new MortarProtocode() {
              @Override
              public JVMCode lower() {
                return new MortarCode().addVarInsn(ALOAD, key);
              }

              @Override
              public TargetCode drop(Context context, Location location) {
                return null;
              }
            }));
  }

  @Override
  public TargetCode drop(Context context, Location location) {
    return null;
  }
}
