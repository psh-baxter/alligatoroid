package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;

public interface MortarHalfType extends SimpleValue {
  Value asValue(MortarProtocode lower);

  default EvaluateResult valueAccess(
      Context context, Location location, Value field, MortarProtocode lower) {
    context.module.errors.add(Error.accessNotSupported(location));
    return EvaluateResult.error;
  }
}
