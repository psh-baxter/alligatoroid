package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.syntax.AtomType;

public class CouldBeInvisible extends BaseKVError {
  public CouldBeInvisible(AtomType type) {
    put("type", type);
  }

  @Override
  protected String description() {
    return "This type needs at least one guaranteed visible front symbol or a primitive (internal assumptions)";
  }
}
