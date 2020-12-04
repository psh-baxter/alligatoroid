package com.zarbosoft.merman.syntax.error;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.merman.editor.Path;

public class AtomTypeDoesntExist extends BaseKVError{

  public AtomTypeDoesntExist(Path typePath, String type) {
    super(ImmutableMap.<String, Object>builder()
      .put("typePath", typePath)
      .put("candidateType", type)
      .build());
  }

  @Override
  protected String name() {
    return "specified candidate type doesn't exist";
  }
}
