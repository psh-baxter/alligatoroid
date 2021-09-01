package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Map;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class StateObject extends BaseState {
  public final TSMap<String, State> fields = new TSMap<>();
  public final ObjectInfo info;

  public StateObject(ObjectInfo info) {
    this.info = info;
  }

  @Override
  public void eatKey(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath, String name) {
    StatePrototype proto = info.fields.getOpt(name);
    if (proto == null) {
      errors.errors.add(Error.deserializeUnknownType(luxemPath, name, fields.keys().toList()));
      stack.add(StateErrorSingle.state);
      return;
    }
    State fieldState = proto.create(errors, luxemPath, stack);
    fields.put(name, fieldState);
    stack.add(fieldState);
  }

  @Override
  public void eatRecordEnd(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath) {
    stack.removeLast();
  }

  @Override
  public Object build(TSList<Error> errors) {
    Object[] args = new Object[fields.size()];
    for (Map.Entry<String, State> field : fields) {
      args[info.argOrder.get(field.getKey())] = field.getValue().build(errors);
    }
    return uncheck(() -> info.constructor.newInstance(args));
  }
}
