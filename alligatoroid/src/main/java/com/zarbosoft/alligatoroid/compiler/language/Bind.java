package com.zarbosoft.alligatoroid.compiler.language;

import com.zarbosoft.alligatoroid.compiler.Binding;
import com.zarbosoft.alligatoroid.compiler.ErrorValue;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.LanguageValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.mortar.WholeValue;
import com.zarbosoft.rendaw.common.ROPair;

public class Bind extends LanguageValue {
  public final Value key;
  public final Value value;

  public Bind(Location id, Value key, Value value) {
    super(id);
    this.key = key;
    this.value = value;
  }

  @Override
  public EvaluateResult evaluate(com.zarbosoft.alligatoroid.compiler.Context context) {
    EvaluateResult.Context ectx = new EvaluateResult.Context(context, location);
    WholeValue key = WholeValue.getWhole(context, location, ectx.evaluate(this.key));
    Value value = ectx.evaluate(this.value);
    if (key == null || value == ErrorValue.error) return EvaluateResult.error;
    Binding old = context.scope.remove((WholeValue) key);
    if (old != null) {
      ectx.record(old.drop(context, location));
    }
    ROPair<EvaluateResult, Binding> bound = value.bind(context, location);
    context.scope.put(key, bound.second);
    return ectx.build(ectx.record(bound.first));
  }
}
