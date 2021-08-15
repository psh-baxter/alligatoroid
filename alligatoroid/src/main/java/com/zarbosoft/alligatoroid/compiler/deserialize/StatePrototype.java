package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.ModuleContext;
import com.zarbosoft.luxem.read.path.LuxemPath;

public interface StatePrototype {
    public State createArray(ModuleContext context, LuxemPath luxemPath, String type);

    public State createRecord(ModuleContext context, LuxemPath luxemPath, String type);

    public State createPrimitive(ModuleContext context, LuxemPath luxemPath, String type);
}
