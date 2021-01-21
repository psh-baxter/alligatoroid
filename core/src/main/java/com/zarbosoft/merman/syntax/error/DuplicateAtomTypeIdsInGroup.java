package com.zarbosoft.merman.syntax.error;

import com.zarbosoft.merman.misc.TSMap;

public class DuplicateAtomTypeIdsInGroup extends BaseKVError {
  public DuplicateAtomTypeIdsInGroup(String group) {
    put("group", group);
  }

  @Override
  protected String name() {
    return "duplicate atom type ids in group";
  }
}
