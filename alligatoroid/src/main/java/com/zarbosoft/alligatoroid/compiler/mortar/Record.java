package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.rendaw.common.ROMap;

public class Record implements MortarValue {
  final ROMap<Object, Value> data;

  public Record(ROMap<Object, Value> data) {
    this.data = data;
  }

  @Override
  public EvaluateResult access(Context context, Location location, Value key0) {
    WholeValue key = WholeValue.getWhole(context, location, key0);
    if (key == null) return EvaluateResult.error;
    Value out = data.getOpt(key.concreteValue());
    if (out == null) {
      context.module.errors.add(Error.noField(location, key));
      return EvaluateResult.error;
    }
    return EvaluateResult.pure(out);
  }
}
