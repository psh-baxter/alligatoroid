package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.OkValue;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMSharedCode;

public interface MortarLowerableValue extends OkValue {
    public JVMSharedCode lower();
}
