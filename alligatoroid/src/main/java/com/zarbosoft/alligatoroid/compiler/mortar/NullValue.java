package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetCode;

public class NullValue implements MortarProtocode, SimpleValue {
  public static final NullValue value = new NullValue();

  private NullValue() {}

  @Override
  public MortarCode lower() {
    return new MortarCode();
  }

  @Override
  public TargetCode drop(Context context, Location location) {
    return null;
  }
}
