package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.ErrorValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROMap;

public class Record extends MortarValue {
  final ROMap<Object, Value> data;

  public Record(ROMap data) {
    this.data = data;
  }

  @Override
  public Value access(Context context, Location location, Value key) {
    if (key == ErrorValue.error) return ErrorValue.error;
    if (key instanceof CompleteValue) {
      Object concreteKey = ((CompleteValue) key).concreteValue();
      Value out = data.get(concreteKey);
      if (out == null) {
        context.errors.add(Error.noField(location, key));
      }
      return out;
    } else {
      // TODO types
      throw new Assertion();
      // return ((Function) type.get("access")).call(context, new Tuple(this, key));
    }
  }

  @Override
  public Value drop(Context context) {
    return NullValue.value;
  }
}
