package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWSharedCode;
import com.zarbosoft.rendaw.common.Assertion;

public class MortarMethodFieldType implements SimpleValue, MortarHalfType {
  public final String name;
  public final String jbcDesc;
  /**
   * Null if null
   */
  public final MortarHalfType returnType;
  public final MortarClass base;

  public MortarMethodFieldType(MortarClass base, String name, String jbcDesc, MortarHalfType returnType) {
    this.base = base;
    this.name = name;
    this.jbcDesc = jbcDesc;
    this.returnType = returnType;
  }

  @Override
  public Value asValue(MortarProtocode lower) {
    return new MortarMethodField(lower, this);
  }

  @Override
  public Value stackAsValue(JVMRWSharedCode code) {
    throw new Assertion();
  }
}
