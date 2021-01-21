package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;

public class GroupChildDoesntExist extends BaseKVError{
  public GroupChildDoesntExist(String group, String child) {
        put("group", group);
        put("child", child);
  }

  @Override
  protected String name() {
    return "specified group child doesn't exist";
  }
}
