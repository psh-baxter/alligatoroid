package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.syntax.AtomType;

import java.util.List;

public class AtomTypeErrors extends BaseKVError {
  public AtomTypeErrors(AtomType atomType, MultiError subErrors) {
    put("atomType", atomType);
    put("subErrors", subErrors);
  }

  @Override
  protected String name() {
    return "atom type suberrors";
  }
}
