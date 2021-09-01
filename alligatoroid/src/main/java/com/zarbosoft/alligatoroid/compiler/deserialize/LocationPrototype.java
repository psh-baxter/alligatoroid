package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.ModuleId;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.TSList;

public class LocationPrototype implements StatePrototype {
  private final ModuleId module;

  public LocationPrototype(ModuleId module) {
    this.module = module;
  }

  @Override
  public State create(TSList<Error> errors, LuxemPath luxemPath, TSList<State> stack) {
    return new StateInt() {
      @Override
      public Object build(TSList<Error> errors) {
        Integer value = (Integer) super.build(errors);
        if (value == null) {
          return null;
        }
        return new Location(module, value);
      }
    };
  }
}
