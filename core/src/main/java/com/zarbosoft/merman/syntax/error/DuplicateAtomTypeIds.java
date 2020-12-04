package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;

public class DuplicateAtomTypeIds extends BaseKVError{

  public DuplicateAtomTypeIds(String id) {
    super(
      ImmutableMap.<String, Object>builder()
        .put("id", id)
        .build());
  }

  @Override
  protected String name() {
    return "duplicate atom type id";
  }
}
