package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMCode;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWCode;

public class NullValue extends MortarValue implements LowerableValue{
  public static final NullValue value = new NullValue();

  @Override
  public Value drop(Context context) {
    return this;
  }

  @Override
  public JVMCode lower() {
    return new JVMRWCode();
  }
}
