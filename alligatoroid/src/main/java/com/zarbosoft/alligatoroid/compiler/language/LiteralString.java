package com.zarbosoft.alligatoroid.compiler.language;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.LanguageValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.mortar.CompleteString;

public class LiteralString extends LanguageValue {
  public final String value;

  public LiteralString(Location id, String value) {
    super(id);
    this.value = value;
  }

  @Override
  public Value evaluate(Context context) {
    return new CompleteString(value);
  }
}
