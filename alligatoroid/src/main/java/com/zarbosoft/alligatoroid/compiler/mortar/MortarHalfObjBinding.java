package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Binding;
import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMCode;
import com.zarbosoft.rendaw.common.ROPair;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;

public class MortarHalfObjBinding implements Binding {
  public final Object key;
  public final MortarHalfType type;

  public MortarHalfObjBinding(Object key, MortarHalfType type) {
    this.key = key;
    this.type = type;
  }

  public static ROPair<EvaluateResult, Binding> bind(
      Context context, Location location, MortarProtocode lower, MortarHalfType type) {
    Object key = new Object();
    return new ROPair<>(
        new EvaluateResult(
            new MortarCode()
                .add(lower.lower())
                .line(context.module.sourceLocation(location))
                .addVarInsn(ASTORE, key),
            NullValue.value),
        new MortarHalfObjBinding(key, type));
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
