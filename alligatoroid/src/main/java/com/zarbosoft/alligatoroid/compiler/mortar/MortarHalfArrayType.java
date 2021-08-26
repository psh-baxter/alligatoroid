package com.zarbosoft.alligatoroid.compiler.mortar;

public class MortarHalfArrayType extends MortarHalfObjectType {

  private final MortarHalfType elementType;

  public MortarHalfArrayType(MortarHalfType elementType) {
    this.elementType = elementType;
  }
}
