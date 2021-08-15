package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Binding;
import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.OkValue;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.rendaw.common.ROPair;

public interface MortarValue extends Binding, OkValue {
  @Override
  public default TargetCode drop(Context context, Location location) {
    return null;
  }

  @Override
  public default ROPair<EvaluateResult, Binding> bind(Context context, Location location) {
    return new ROPair<>(new EvaluateResult(null, this), this);
  }

  @Override
  public default EvaluateResult fork(Context context, Location location) {
    return new EvaluateResult(null, this);
  }
}
