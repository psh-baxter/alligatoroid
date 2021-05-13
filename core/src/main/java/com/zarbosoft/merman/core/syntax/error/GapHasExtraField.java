package com.zarbosoft.merman.core.syntax.error;

public class GapHasExtraField extends BaseKVError {
  public GapHasExtraField(String gapId, String field) {
    put("gap", gapId);
    put("field", field);
  }

  @Override
  protected String description() {
    return "gap has extra field";
  }
}
