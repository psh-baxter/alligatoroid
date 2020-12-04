package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.merman.syntax.back.BaseBackArraySpec;

public class ArrayMissingAtom extends BaseKVError{
  public ArrayMissingAtom(BaseBackArraySpec spec) {
    super(ImmutableMap.<String, Object>builder().put("spec", spec).build());
  }

  @Override
  protected String name() {
    return "array element spec doesn't contain atom spec";
  }
}
