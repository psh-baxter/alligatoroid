package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.merman.syntax.Direction;

public class NotTransverse extends BaseKVError{

  public NotTransverse(Direction converseDirection, Direction transverseDirection) {
        put("converse", converseDirection);
        put("transverse", transverseDirection);
  }

  @Override
  protected String name() {
    return "converse and transverse directions do not cross";
  }
}
