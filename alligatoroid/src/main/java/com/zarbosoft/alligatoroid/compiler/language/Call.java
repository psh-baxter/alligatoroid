package com.zarbosoft.alligatoroid.compiler.language;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.LanguageValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;

public class Call extends LanguageValue {
  public final Value target;
  public final Value argument;

  public Call(Location id, Value target, Value argument) {
    super(id, hasLowerInSubtree(target, argument));
    this.target = target;
    this.argument = argument;
  }

  @Override
  public EvaluateResult evaluate(Context context) {
    EvaluateResult.Context ectx = new EvaluateResult.Context(context, location);
    return ectx.build(
        ectx.record(ectx.evaluate(target).call(context, location, ectx.evaluate(argument))));
  }
}
