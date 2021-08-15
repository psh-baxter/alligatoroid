package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.ModuleContext;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.ModuleId;
import com.zarbosoft.luxem.read.path.LuxemPath;

public class LocationPrototype extends BaseStatePrototype {
  private final ModuleId module;

  public LocationPrototype(ModuleId module) {
    this.module = module;
  }

  @Override
  public State createPrimitive(ModuleContext context, LuxemPath luxemPath, String type) {
    return new StateInt() {
      @Override
      public Object build(ModuleContext context) {
        Integer value = (Integer) super.build(context);
        if (value == null) {
          return null;
        }
        return new Location(module, value);
      }
    };
  }
}
