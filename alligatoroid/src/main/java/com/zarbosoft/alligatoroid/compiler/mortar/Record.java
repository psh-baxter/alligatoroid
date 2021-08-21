package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.rendaw.common.ROMap;

public class Record implements SimpleValue {
  public final ROMap<Object, Value> data;

  public Record(ROMap<Object, Value> data) {
    this.data = data;
  }
}
