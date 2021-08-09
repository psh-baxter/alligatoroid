package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.TSList;

public class ArrayStatePrototype extends BaseStatePrototype {
  private final StatePrototype inner;

  public ArrayStatePrototype(StatePrototype inner) {
    this.inner = inner;
  }

  @Override
  public State createArray(Context context, LuxemPath luxemPath, String type) {
    return new BaseNonPrimitiveState() {
      TSList<State> elements = new TSList<>();

      @Override
      public void createdState(String key, State state) {
        elements.add(state);
      }

      @Override
      public void eatArrayBegin(Context context, TSList<State> stack, LuxemPath luxemPath) {
        proto = inner;
        super.eatArrayBegin(context, stack, luxemPath);
      }

      @Override
      public void eatRecordBegin(Context context, TSList<State> stack, LuxemPath luxemPath) {
        proto = inner;
        super.eatRecordBegin(context, stack, luxemPath);
      }

      @Override
      public void eatPrimitive(Context context, TSList<State> stack, LuxemPath luxemPath, String value) {
        proto = inner;
        super.eatPrimitive(context, stack, luxemPath, value);
      }

      @Override
      public Object build(Context context) {
        if (!ok) return null;
        TSList<Object> out = new TSList<>();
        for (State element : elements) {
          Object value = element.build(context);
          if (value == null) return null;
          out.add(value);
        }
        return out;
      }
    };
  }
}
