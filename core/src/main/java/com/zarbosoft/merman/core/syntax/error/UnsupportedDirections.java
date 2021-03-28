package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.syntax.Direction;

public class UnsupportedDirections extends BaseKVError {
  public UnsupportedDirections(Direction converseDirection, Direction transverseDirection) {
    this.put("converse", converseDirection);
    this.put("transverse", transverseDirection);
  }

  @Override
  protected String description() {
    return "Unsupported direction combination";
  }
}
