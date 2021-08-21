package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWSharedCode;

public interface MortarHalfType extends SimpleValue {
  public Value asValue(MortarProtocode lower);

  public Value stackAsValue(JVMRWSharedCode code);
}
