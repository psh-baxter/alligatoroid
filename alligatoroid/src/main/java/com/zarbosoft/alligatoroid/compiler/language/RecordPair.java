package com.zarbosoft.alligatoroid.compiler.language;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.LanguageValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.rendaw.common.Assertion;

public class RecordPair extends LanguageValue {
  public final Value key;
  public final Value value;

  public RecordPair(Location id, Value key, Value value) {
    super(id);
    this.key = key;
    this.value = value;
  }

  @Override
  public EvaluateResult evaluate(Context context) {
    // Evaluated by record literal directly
    throw new Assertion();
  }
}
