package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWSharedCode;

public class JVMCode extends JVMRWSharedCode {
  public static final String JVM_TARGET_NAME = "jvm";

  @Override
  public String targetName() {
    return JVM_TARGET_NAME;
  }
}
