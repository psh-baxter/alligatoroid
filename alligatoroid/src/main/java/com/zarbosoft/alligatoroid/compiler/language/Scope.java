package com.zarbosoft.alligatoroid.compiler.language;

import com.zarbosoft.alligatoroid.compiler.Binding;
import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.LanguageValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.rendaw.common.ReverseIterable;
import com.zarbosoft.rendaw.common.TSList;

public class Scope extends LanguageValue {
  public final Value inner;

  public Scope(Location id, Value inner) {
    super(id, hasLowerInSubtree(inner));
    this.inner = inner;
  }

  @Override
  public EvaluateResult evaluate(Context context) {
    Context subcontext = context.pushScope();
    EvaluateResult res = inner.evaluate(subcontext);
    TSList<TargetCode> pre = new TSList<>(res.preEffect);
    for (Binding binding : new ReverseIterable<>(subcontext.scope.atLevel())) {
      pre.add(binding.drop(context, location));
    }
    return new EvaluateResult(
        context.target.merge(context, location, pre), res.postEffect, res.value);
  }
}
