package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.TSList;

public interface State {
  void eatArrayBegin(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath);

  void eatArrayEnd(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath);

  void eatRecordBegin(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath);

  void eatRecordEnd(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath);

  void eatKey(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath, String name);

  void eatType(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath, String name);

  void eatPrimitive(TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath, String value);

  public Object build(TSList<Error> errors);
}
