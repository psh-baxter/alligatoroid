package com.zarbosoft.alligatoroid.compiler.language;

import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.LanguageValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;

public class Access extends LanguageValue {
  public final Value base;
  public final Value key;

  public Access(Location id, Value base, Value key) {
    super(id);
    this.base = base;
    this.key = key;
  }

  @Override
  public EvaluateResult evaluate(com.zarbosoft.alligatoroid.compiler.Context context) {
    EvaluateResult.Context ectx = new EvaluateResult.Context(context, location);
    return ectx.build(
        ectx.record(ectx.evaluate(this.base).access(context, location, ectx.evaluate(this.key))));
  }
}
