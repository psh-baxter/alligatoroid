package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;

public class DuplicateAtomTypeIdsInGroup extends BaseKVError{
  public DuplicateAtomTypeIdsInGroup(String group) {
    super(
      ImmutableMap.<String, Object>builder()
        .put("group", group)
        .build());
  }

  @Override
  protected String name() {
    return "duplicate atom type ids in group";
  }
}
