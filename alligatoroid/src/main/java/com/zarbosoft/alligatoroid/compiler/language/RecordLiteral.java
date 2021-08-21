package com.zarbosoft.alligatoroid.compiler.language;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.LanguageValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.mortar.Record;
import com.zarbosoft.alligatoroid.compiler.mortar.WholeValue;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSMap;

public class RecordLiteral extends LanguageValue {
  public final ROList<Value> elements;

  public RecordLiteral(Location id, ROList<Value> elements) {
    super(id);
    this.elements = elements;
  }

  @Override
  public EvaluateResult evaluate(Context context) {
    EvaluateResult.Context ectx = new EvaluateResult.Context(context, location);
    TSMap<Object, Value> data = new TSMap<>();
    for (Value element : elements) {
      if (!(element instanceof RecordPair)) {
        context.module.errors.add(
            Error.notRecordPair(
                ((LanguageValue) element).location, element.getClass().getSimpleName()));
        continue;
      }
      WholeValue key =
          WholeValue.getWhole(context, location, ectx.evaluate(((RecordPair) element).key));
      Value value = ectx.evaluate(((RecordPair) element).value);
      data.putNew(key.concreteValue(), value);
    }
    return ectx.build(new Record(data));
  }
}
