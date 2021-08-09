package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.luxem.read.path.LuxemPath;

public interface StatePrototype {
    public State createArray(Context context, LuxemPath luxemPath, String type);

    public State createRecord(Context context, LuxemPath luxemPath, String type);

    public State createPrimitive(Context context, LuxemPath luxemPath, String type);
}
