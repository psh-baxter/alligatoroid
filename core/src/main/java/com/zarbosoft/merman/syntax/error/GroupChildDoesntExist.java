package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;

public class GroupChildDoesntExist extends BaseKVError{
  public GroupChildDoesntExist(String group, String child) {
    super(
      ImmutableMap.<String, Object>builder()
        .put("group", group)
        .put("child", child)
        .build());
  }

  @Override
  protected String name() {
    return "specified group child doesn't exist";
  }
}
