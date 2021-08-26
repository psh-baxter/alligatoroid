package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Value;

public class MortarMethodFieldType implements SimpleValue, MortarHalfType {
  public final String name;
  public final String jbcDesc;
  /** Null if null */
  public final MortarHalfDataType returnType;

  public final MortarClass base;

  public MortarMethodFieldType(
      MortarClass base, String name, String jbcDesc, MortarHalfDataType returnType) {
    this.base = base;
    this.name = name;
    this.jbcDesc = jbcDesc;
    this.returnType = returnType;
  }

  @Override
  public Value asValue(MortarProtocode lower) {
    return new MortarMethodField(lower, this);
  }
}
