package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.TSList;

public interface State {
    void eatArrayBegin(Context context, TSList<State> stack, LuxemPath luxemPath);

    void eatArrayEnd(Context context, TSList<State> stack, LuxemPath luxemPath);

    void eatRecordBegin(Context context, TSList<State> stack, LuxemPath luxemPath);

    void eatRecordEnd(Context context, TSList<State> stack, LuxemPath luxemPath);

    void eatKey(Context context, TSList<State> stack, LuxemPath luxemPath, String name);

    void eatType(Context context, TSList<State> stack, LuxemPath luxemPath, String name);

    void eatPrimitive(Context context, TSList<State> stack, LuxemPath luxemPath, String value);

    public Object build(Context context);
}
