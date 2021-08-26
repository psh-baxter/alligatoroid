package com.zarbosoft.alligatoroid.compiler.language;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.LanguageValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.mortar.LooseRecord;
import com.zarbosoft.alligatoroid.compiler.mortar.WholeValue;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSOrderedMap;

public class Record extends LanguageValue {
  public final ROList<Value> elements;

  public Record(Location id, ROList<Value> elements) {
    super(id, hasLowerInSubtree(elements));
    this.elements = elements;
  }

  @Override
  public EvaluateResult evaluate(Context context) {
    TSOrderedMap<Object, EvaluateResult> data = new TSOrderedMap<>();
    for (Value element : elements) {
      if (!(element instanceof RecordElement)) {
        context.module.errors.add(
            Error.notRecordPair(
                ((LanguageValue) element).location, element.getClass().getSimpleName()));
        continue;
      }
      EvaluateResult keyRes = ((RecordElement) element).key.evaluate(context);
      WholeValue key = WholeValue.getWhole(context, location, keyRes.value);
      if (key == null) return EvaluateResult.error;
      EvaluateResult valueRes = ((RecordElement) element).value.evaluate(context);
      data.put(
          key.concreteValue(),
          new EvaluateResult(
              context.target.merge(
                  context, location, new TSList<>(keyRes.preEffect, valueRes.preEffect)),
              valueRes.postEffect,
              valueRes.value));
    }
    return new EvaluateResult(null, null, new LooseRecord(data));
  }
}
