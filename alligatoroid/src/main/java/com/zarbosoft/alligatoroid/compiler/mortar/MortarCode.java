package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWCode;

public class MortarCode extends JVMRWCode {
  public static final String MORTAR_TARGET_NAME = "mortar";

  @Override
  public String targetName() {
    return MORTAR_TARGET_NAME;
  }
}
