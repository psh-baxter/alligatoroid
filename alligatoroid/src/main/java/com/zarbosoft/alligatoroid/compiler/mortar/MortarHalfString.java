package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Binding;
import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMSharedCode;
import com.zarbosoft.rendaw.common.ROPair;

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
    return MortarHalfObjBinding.bind(context, location, lower, new MortarHalfStringType());
  }

  @Override
  public JVMSharedCode lower() {
    return lower.lower();
  }
}
