package com.zarbosoft.alligatoroid.compiler.language;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.LanguageValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;

public final class Lower extends LanguageValue {
  public final Value child;

  public Lower(Location id, Value child) {
    super(id, true);
    this.child = child;
  }

  @Override
  public EvaluateResult evaluate(Context context) {
    context.module.errors.add(Error.lowerTooDeep(location));
    return EvaluateResult.error;
  }
}
