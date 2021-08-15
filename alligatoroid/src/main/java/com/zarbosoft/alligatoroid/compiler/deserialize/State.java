package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.ModuleContext;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.TSList;

public interface State {
    void eatArrayBegin(ModuleContext context, TSList<State> stack, LuxemPath luxemPath);

    void eatArrayEnd(ModuleContext context, TSList<State> stack, LuxemPath luxemPath);

    void eatRecordBegin(ModuleContext context, TSList<State> stack, LuxemPath luxemPath);

    void eatRecordEnd(ModuleContext context, TSList<State> stack, LuxemPath luxemPath);

    void eatKey(ModuleContext context, TSList<State> stack, LuxemPath luxemPath, String name);

    void eatType(ModuleContext context, TSList<State> stack, LuxemPath luxemPath, String name);

    void eatPrimitive(ModuleContext context, TSList<State> stack, LuxemPath luxemPath, String value);

    public Object build(ModuleContext context);
}
