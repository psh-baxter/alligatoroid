package com.zarbosoft.alligatoroid.compiler.language;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.LanguageValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.mortar.WholeString;

public class LiteralString extends LanguageValue {
  public final String value;

  public LiteralString(Location id, String value) {
    super(id);
    this.value = value;
  }

  @Override
  public EvaluateResult evaluate(Context context) {
    return EvaluateResult.pure(new WholeString(value));
  }
}
