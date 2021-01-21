package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.merman.syntax.back.BackAtomSpec;
import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.merman.syntax.back.BaseBackArraySpec;

public class ArrayMultipleAtoms extends BaseKVError{
  public ArrayMultipleAtoms(BaseBackArraySpec spec, BackAtomSpec first, BackSpec second) {
      put("spec", spec);
      put("first", first);
      put("second", second);
  }

  @Override
  protected String name() {
    return "array element spec contains more than one atom spec";
  }
}
