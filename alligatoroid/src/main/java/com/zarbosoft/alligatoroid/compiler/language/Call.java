package com.zarbosoft.alligatoroid.compiler.language;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.LanguageValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;

public class Call extends LanguageValue {
  public final Value target;
  public final Value argument;

  public Call(Location id, Value target, Value argument) {
    super(id);
    this.target = target;
    this.argument = argument;
  }

  @Override
  public Value evaluate(Context context) {
    Value target = this.target.evaluate(context);
    Value argument = this.argument.evaluate(context);
    return target.call(context, location, argument);
  }
}
