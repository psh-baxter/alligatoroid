package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMCode;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWCode;

public class NullValue implements MortarProtocode, MortarValue {
  public static final NullValue value = new NullValue();

  private NullValue() {}

  @Override
  public JVMCode lower() {
    return new MortarCode();
  }

  @Override
  public TargetCode drop(Context context, Location location) {
    return null;
  }
}
