package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Binding;
import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetCode;

public class MortarHalfBinding implements Binding {
  public final Object key;
  public final MortarHalfDataType type;

  public MortarHalfBinding(Object key, MortarHalfDataType type) {
    this.key = key;
    this.type = type;
  }

  @Override
  public EvaluateResult fork(Context context, Location location) {
    return EvaluateResult.pure(
        type.asValue(
            new MortarProtocode() {
              @Override
              public MortarCode lower() {
                return (MortarCode) new MortarCode().addVarInsn(type.loadOpcode(), key);
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
