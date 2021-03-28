package com.zarbosoft.merman.core.syntax.error;

public class DuplicateAtomTypeIds extends BaseKVError{

  public DuplicateAtomTypeIds(String id) {
        put("id", id);
  }

  @Override
  protected String description() {
    return "duplicate atom type id";
  }
}
