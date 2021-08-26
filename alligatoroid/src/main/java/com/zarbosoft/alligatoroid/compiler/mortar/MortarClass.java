package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.rendaw.common.ROMap;

public class MortarClass extends MortarHalfObjectType implements SimpleValue {
  public final String jvmInternalClass;
  public ROMap<Object, MortarHalfType> fields;

  public MortarClass(String jvmInternalClass) {
    this.jvmInternalClass = jvmInternalClass;
  }

  @Override
  public EvaluateResult valueAccess(
      Context context, Location location, Value field0, MortarProtocode lower) {
    WholeValue key = WholeValue.getWhole(context, location, field0);
    MortarHalfType field = fields.getOpt(key.concreteValue());
    if (field == null) {
      context.module.errors.add(Error.noField(location, key));
      return EvaluateResult.error;
    }
    return EvaluateResult.pure(field.asValue(lower));
  }
}
