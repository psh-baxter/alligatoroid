package com.zarbosoft.alligatoroid.compiler.language;

import com.zarbosoft.alligatoroid.compiler.Binding;
import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.LanguageValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.mortar.NullValue;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ReverseIterable;

public class Block extends LanguageValue {
  public final ROList<Value> children;

  public Block(Location id, ROList<Value> children) {
    super(id);
    this.children = children;
  }

  public static EvaluateResult evaluate(
      Context context, Location location, ROList<Value> children) {
    context = context.pushScope();
    EvaluateResult.Context ectx = new EvaluateResult.Context(context, location);
    Value last = null;
    Location lastLocation = null;
    for (Value child : children) {
      if (last != null) ectx.record(last.drop(context, lastLocation));
      lastLocation = child.location();
      last = ectx.evaluate(child);
    }
    if (last == null) {
      last = NullValue.value;
    }
    for (Binding binding : new ReverseIterable<>(context.scope.atLevel())) {
      ectx.record(binding.drop(context, location));
    }
    return ectx.buildScoped(last);
  }

  @Override
  public EvaluateResult evaluate(Context context) {
    return evaluate(context, location, children);
  }
}
