package com.zarbosoft.alligatoroid.compiler.language;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.LanguageValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.mortar.NullValue;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

public class Block extends LanguageValue {
  public final ROList<Value> children;

  public Block(Location id, ROList<Value> children) {
    super(id);
    this.children = children;
  }

  public static Value evaluate(Context context, ROList<Value> children) {
    Value last = null;
    for (Value child : children) {
      Value evaluated = child.evaluate(context);
      if (last != null) {
        last = last.drop(context);
        Value temp = evaluated.mergePrevious(context, last);
        if (temp == null) {
          temp = last.mergeNext(context, evaluated);
        }
        last = temp;
      } else {
        last = evaluated;
      }
    }
    if (last == null) {
      last = NullValue.value;
    }
    return last;
  }

  @Override
  public Value evaluate(Context context) {
    return evaluate(context, children);
  }
}
