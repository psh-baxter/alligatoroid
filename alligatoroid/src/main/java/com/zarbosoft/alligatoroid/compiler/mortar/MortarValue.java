package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.OkValue;
import com.zarbosoft.alligatoroid.compiler.Value;

public abstract class MortarValue extends OkValue {
    @Override
    public Value drop(Context context) {
        return NullValue.value;
    }
}
