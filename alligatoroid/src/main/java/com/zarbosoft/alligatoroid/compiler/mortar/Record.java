package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.rendaw.common.ROMap;

public class Record {
  public final ROMap<Object, Object> data;

  public Record(ROMap<Object, Object> data) {
    this.data = data;
  }

  /*
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
   */
}
