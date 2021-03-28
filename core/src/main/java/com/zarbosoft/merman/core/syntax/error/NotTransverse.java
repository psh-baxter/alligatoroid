package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.syntax.Direction;

public class NotTransverse extends BaseKVError{

  public NotTransverse(Direction converseDirection, Direction transverseDirection) {
        put("converse", converseDirection);
        put("transverse", transverseDirection);
  }

  @Override
  protected String description() {
    return "converse and transverse directions do not cross";
  }
}
