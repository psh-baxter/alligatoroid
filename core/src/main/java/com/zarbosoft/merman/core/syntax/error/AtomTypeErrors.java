package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.misc.MultiError;
import com.zarbosoft.merman.core.syntax.AtomType;

public class AtomTypeErrors extends BaseKVError {
  public AtomTypeErrors(AtomType atomType, MultiError subErrors) {
    put("atomType", atomType.id());
    put("subErrors", subErrors);
  }

  @Override
  protected String description() {
    return "atom type suberrors";
  }
}
