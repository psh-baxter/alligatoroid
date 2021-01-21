package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;

import java.util.List;

public class TypeCircularReference extends BaseKVError{

  public TypeCircularReference(List<String> subpath) {
        put("subpath", subpath);
  }

  @Override
  protected String name() {
    return "type circular reference";
  }
}
