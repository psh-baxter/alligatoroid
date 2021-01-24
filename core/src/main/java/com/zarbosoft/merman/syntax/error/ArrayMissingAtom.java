package com.zarbosoft.merman.syntax.error;

import com.zarbosoft.merman.syntax.back.BaseBackArraySpec;

public class ArrayMissingAtom extends BaseKVError{
  public ArrayMissingAtom(BaseBackArraySpec spec) {
    put("spec", spec);
  }

  @Override
  protected String name() {
    return "array element spec doesn't contain atom spec";
  }
}
