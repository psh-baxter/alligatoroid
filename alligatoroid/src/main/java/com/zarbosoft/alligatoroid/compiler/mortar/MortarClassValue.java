package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Binding;
import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.OkValue;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.rendaw.common.ROPair;

public class MortarClassValue implements OkValue {
  private final MortarProtocode lower;
  private final MortarClass type;

  public MortarClassValue(MortarProtocode lower, MortarClass type) {
    this.lower = lower;
    this.type = type;
  }

  @Override
  public TargetCode drop(Context context, Location location) {
    return lower.drop(context, location);
  }

  @Override
  public EvaluateResult access(Context context, Location location, Value field0) {
    WholeValue key = WholeValue.getWhole(context, location, field0);
    MortarHalfType field = type.fields.getOpt(key.concreteValue());
    if (field == null) {
      context.module.errors.add(Error.noField(location, key));
      return EvaluateResult.error;
    }
    return EvaluateResult.pure(field.asValue(lower));
  }

  @Override
  public ROPair<EvaluateResult, Binding> bind(Context context, Location location) {
    return MortarHalfObjBinding.bind(context,location, lower, type);
  }
}
