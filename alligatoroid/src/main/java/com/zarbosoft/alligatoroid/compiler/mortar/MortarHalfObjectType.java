package com.zarbosoft.alligatoroid.compiler.mortar;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;

public class MortarHalfObjectType implements MortarHalfDataType {
  public static MortarHalfObjectType type = new MortarHalfObjectType();

  protected MortarHalfObjectType() {}

  @Override
  public int storeOpcode() {
    return ASTORE;
  }

  @Override
  public int loadOpcode() {
    return ALOAD;
  }
}
