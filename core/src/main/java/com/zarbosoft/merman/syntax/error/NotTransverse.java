package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.merman.syntax.Syntax;

public class NotTransverse extends BaseKVError{

  public NotTransverse(Syntax.Direction converseDirection, Syntax.Direction transverseDirection) {
    super(
      ImmutableMap.<String, Object>builder()
        .put("converse", converseDirection)
        .put("transverse", transverseDirection)
        .build());
  }

  @Override
  protected String name() {
    return "converse and transverse directions do not cross";
  }
}
