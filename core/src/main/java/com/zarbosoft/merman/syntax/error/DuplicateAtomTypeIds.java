package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;

public class DuplicateAtomTypeIds extends BaseKVError{

  public DuplicateAtomTypeIds(String id) {
        put("id", id);
  }

  @Override
  protected String name() {
    return "duplicate atom type id";
  }
}
