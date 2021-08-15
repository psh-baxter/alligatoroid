package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.OkValue;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMCode;

public interface MortarLowerableValue extends OkValue {
    public JVMCode lower();
}
