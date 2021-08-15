package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWCode;

public interface MortarHalfType extends MortarValue {
  public Value asValue(MortarProtocode lower);

  public Value stackAsValue(JVMRWCode code);
}
