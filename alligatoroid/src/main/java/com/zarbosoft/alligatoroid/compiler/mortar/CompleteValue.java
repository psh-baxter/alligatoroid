package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Value;

public interface CompleteValue extends Value, LowerableValue {
    Object concreteValue();
}
