package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;

import java.util.Set;

public class UnusedBackData extends BaseKVError {

  public UnusedBackData(Set<String> unused) {
    put("unused", unused);
  }

  @Override
  protected String name() {
    return "unused data from back fields";
  }
}
