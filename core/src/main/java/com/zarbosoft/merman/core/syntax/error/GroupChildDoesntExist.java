package com.zarbosoft.merman.core.syntax.error;

public class GroupChildDoesntExist extends BaseKVError {
  public GroupChildDoesntExist(String group, String child) {
    put("group", group);
    put("child", child);
  }

  @Override
  protected String description() {
    return "specified group child doesn't exist";
  }
}
