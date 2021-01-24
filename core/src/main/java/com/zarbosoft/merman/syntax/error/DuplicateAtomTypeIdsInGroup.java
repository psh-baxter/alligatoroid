package com.zarbosoft.merman.syntax.error;

public class DuplicateAtomTypeIdsInGroup extends BaseKVError {
  public DuplicateAtomTypeIdsInGroup(String group) {
    put("group", group);
  }

  @Override
  protected String name() {
    return "duplicate atom type ids in group";
  }
}
