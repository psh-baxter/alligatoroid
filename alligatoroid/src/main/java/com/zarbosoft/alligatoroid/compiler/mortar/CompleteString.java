package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.OkValue;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMCode;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWCode;

public class CompleteString extends OkValue implements CompleteValue {
  public final String value;

  public CompleteString(String value) {
    this.value = value;
  }

  @Override
  public JVMCode lower() {
    return new JVMRWCode().addString(value);
  }

  @Override
  public Value drop(Context context) {
    return NullValue.value;
  }

  @Override
  public Object concreteValue() {
    return value;
  }
}
