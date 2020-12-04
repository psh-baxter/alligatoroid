package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;

import java.util.Set;

public class UnusedBackData extends BaseKVError{

  public UnusedBackData(Set<String> unused) {
    super(
      ImmutableMap.<String, Object>builder()
        .put("unused", unused)
        .build());
  }

  @Override
  protected String name() {
    return "unused data from back fields";
  }
}
