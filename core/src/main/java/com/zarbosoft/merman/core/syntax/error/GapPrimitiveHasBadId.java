package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.syntax.GapAtomType;

public class GapPrimitiveHasBadId extends BaseKVError {
  public GapPrimitiveHasBadId(String gapId, String field) {
    put("gap", gapId);
    put("id", field);
    put("must_be", GapAtomType.PRIMITIVE_KEY);
  }

  @Override
  protected String description() {
    return "gap primitive has incorrect id";
  }
}
