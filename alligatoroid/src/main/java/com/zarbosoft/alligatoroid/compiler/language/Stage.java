package com.zarbosoft.alligatoroid.compiler.language;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.LanguageValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class Stage extends LanguageValue {
  public final Value child;

  public Stage(Location id, Value child) {
    super(id, hasLowerInSubtree(child));
    this.child = child;
  }

  public static EvaluateResult stageLower(Context context, Value value) {
    if (value instanceof Lower) {
      Lower lower = (Lower) value;
      return lower.child.evaluate(context);
    } else if (value instanceof LanguageValue) {
      LanguageValue languageValue = (LanguageValue) value;
      if (languageValue.hasLowerInSubtree) {
        EvaluateResult.Context ectx = new EvaluateResult.Context(context, languageValue.location);
        Constructor<?> constructor = value.getClass().getConstructors()[0];
        Parameter[] parameters = constructor.getParameters();
        Object[] parameterValues = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
          Parameter parameter = parameters[i];
          if (parameter.getType() == Location.class) {
            Object parameterValue = uncheck(() -> value.getClass().getField("location").get(value));
            parameterValues[i] = parameterValue;
          } else if (ROList.class.isAssignableFrom(parameter.getType())) {
            Object parameterValue =
                uncheck(() -> value.getClass().getField(parameter.getName()).get(value));
            TSList valueCopy = new TSList();
            for (Object o : ((TSList) parameterValue)) {
              valueCopy.add(ectx.record(stageLower(context, (Value) o)));
            }
            parameterValues[i] = valueCopy;
          } else if (Value.class.isAssignableFrom(parameter.getType())) {
            Object parameterValue =
                uncheck(() -> value.getClass().getField(parameter.getName()).get(value));
            parameterValues[i] = ectx.record(stageLower(context, (Value) parameterValue));
          } else throw new Assertion();
        }
        return ectx.build(uncheck(() -> (Value) constructor.newInstance(parameterValues)));
      } else {
        return EvaluateResult.pure(value);
      }
    } else {
      return EvaluateResult.pure(value);
    }
  }

  @Override
  public EvaluateResult evaluate(Context context) {
    return stageLower(context, child);
  }
}
