package com.zarbosoft.merman.core.syntax.error;

public class DuplicateAtomTypeIdsInGroup extends BaseKVError {
  public DuplicateAtomTypeIdsInGroup(String group) {
    put("group", group);
  }

  @Override
  protected String description() {
    return "duplicate atom type ids in group";
  }
}
