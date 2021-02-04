package com.zarbosoft.merman.syntax.error;

public class DuplicateBackId extends BaseKVError {
  public DuplicateBackId(String id) {
    put("id", id);
  }

  @Override
  protected String description() {
    return "duplicate back ids in atom";
  }
}
