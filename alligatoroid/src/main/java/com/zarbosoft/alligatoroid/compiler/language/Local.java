package com.zarbosoft.alligatoroid.compiler.language;

import com.zarbosoft.alligatoroid.compiler.Binding;
import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.LanguageValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.mortar.WholeValue;

public class Local extends LanguageValue {
  public final Value key;

  public Local(Location id, Value key) {
    super(id);
    this.key = key;
  }

  @Override
  public EvaluateResult evaluate(Context context) {
    EvaluateResult.Context ectx = new EvaluateResult.Context(context, location);
    WholeValue key = WholeValue.getWhole(context, location, ectx.evaluate(this.key));
    if (key == null) return EvaluateResult.error;
    Binding value = context.scope.get(key);
    if (value == null) {
      context.module.errors.add(Error.noField(location, key));
      return EvaluateResult.error;
    }
    return ectx.build(ectx.record(value.fork(context, location)));
  }
}
