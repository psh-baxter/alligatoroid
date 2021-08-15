package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Binding;
import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMCode;
import com.zarbosoft.rendaw.common.ROPair;

import static org.objectweb.asm.Opcodes.ASTORE;

public class MortarHalfString implements MortarLowerableValue {
  private final MortarProtocode lower;

  public MortarHalfString(MortarProtocode lower) {
    this.lower = lower;
  }

  @Override
  public TargetCode drop(Context context, Location location) {
    return lower.drop(context, location);
  }

  @Override
  public ROPair<EvaluateResult, Binding> bind(Context context, Location location) {
    Object key = new Object();
    return new ROPair<>(
        new EvaluateResult(
            new MortarCode().add(lower.lower()).addVarInsn(ASTORE, key), NullValue.value),
        new MortarHalfObjBinding(key, new MortarHalfStringType()));
  }

  @Override
  public JVMCode lower() {
    return lower.lower();
  }
}
