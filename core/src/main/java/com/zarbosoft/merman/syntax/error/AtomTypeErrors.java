package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.merman.syntax.AtomType;

import java.util.List;

public class AtomTypeErrors extends BaseKVError{
  public AtomTypeErrors(AtomType atomType, List<Object> subErrors) {
    super(ImmutableMap.<String, Object>builder()
      .put("atomType", atomType)
      .put("subErrors", subErrors)
      .build());
  }

  @Override
  protected String name() {
    return "atom type suberrors";
  }
}
