package com.zarbosoft.alligatoroid.compiler.language;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.LanguageValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;

public class Access extends LanguageValue {
  public final Value base;
  public final Value field;

  public Access(Location id, Value base, Value field) {
    super(id);
    this.base = base;
    this.field = field;
  }

  @Override
  public Value evaluate(Context context) {
    Value base = this.base.evaluate(context);
    Value field = this.field.evaluate(context);
    return base.access(context, location, field);
  }
}
