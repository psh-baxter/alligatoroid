package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;

import java.util.List;

public class TypeCircularReference extends BaseKVError{

  public TypeCircularReference(List<String> subpath) {
    super(
      ImmutableMap.<String, Object>builder()
        .put("subpath", subpath)
        .build());
  }

  @Override
  protected String name() {
    return "type circular reference";
  }
}
